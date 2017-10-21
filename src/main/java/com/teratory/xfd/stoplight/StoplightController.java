package com.teratory.xfd.stoplight;

import com.comsysto.buildlight.cleware.driver.TrafficLight;
import com.comsysto.buildlight.cleware.driver.TrafficLightFactory;
import com.teratory.xfd.CompletionStatus;
import com.teratory.xfd.ContinuousIntegrationProjectSet;
import com.teratory.xfd.ContinuousIntegrationProjectState;
import com.teratory.xfd.RunningStatus;
import com.teratory.xfd.jenkins.JenkinsConnector;
import com.teratory.xfd.jenkins.JenkinsProjectSet;
import com.teratory.xfd.stoplight.driver.ExternalProcessTrafficLight;
import com.teratory.xfd.stoplight.driver.PrintStreamMockTrafficLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * The main MVC controller class interacting with the Jenkins state (data) and reflecting it on the stoplight (view).
 *
 * This class is used as the Main-Class for the output Jar file.
 *
 * @author jstokes
 */
public class StoplightController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoplightController.class);

    /** How often we poll Jenkins for new state, in milliseconds. */
    private static long MINIMUM_TIME_BETWEEN_ITERATIONS = 1000L;

    /** The longest we'll wait, in milliseconds, between polls. */
    private static long MAXIMUM_TIME_BETWEEN_ITERATIONS = 15000L;

    /** The number of polls we'll make before logging something to say we're still alive. */
    private static int ITERATIONS_BETWEEN_ALIVE_INFO = 1000;

    /** The main method of this class. */
    public static void main(String[] args) throws IOException {
        // These Innotas-specific default values will only work from within the Innotas dev VPN.
        String baseUrl = args.length > 0 ? args[0] : "http://ci1s2.innotas.net/jenkins/";
        String highPriorityViewUrlSuffix = args.length > 1 ? args[1] : "view/Priority-High/";
        String mediumPriorityViewUrlSuffix = args.length > 2 ? args[2] : "view/Priority-Medium/";

        URL highPriorityJenkinsView = new URL(baseUrl + highPriorityViewUrlSuffix);
        URL mediumPriorityJenkinsView = new URL(baseUrl + mediumPriorityViewUrlSuffix);

        TrafficLight driver = createTrafficLightDriver();
        try (Stoplight stoplight = new Stoplight(driver)) {
            StoplightController controller = new StoplightController(stoplight, highPriorityJenkinsView, mediumPriorityJenkinsView);
            controller.run();
        }
    }

    /** Creates the driver class for the USB stoplight. */
    private static TrafficLight createTrafficLightDriver() throws IOException {
        if ("true".equalsIgnoreCase(System.getProperty("consoleOnly"))) return new PrintStreamMockTrafficLight(System.out);
        else if (false) return TrafficLightFactory.createNewInstance();
        else return new ExternalProcessTrafficLight();
    }

    private URL jenkinsHighPriorityViewUrl;
    private URL jenkinsMediumPriorityViewUrl;
    private Stoplight stoplight;
    private long timeBetweenIterations = MINIMUM_TIME_BETWEEN_ITERATIONS;
    private JenkinsConnector connector;

    public StoplightController(Stoplight stoplight, URL jenkinsHighPriorityViewUrl, URL jenkinsMediumPriorityViewUrl) {
        this(stoplight, jenkinsHighPriorityViewUrl, jenkinsMediumPriorityViewUrl, new JenkinsConnector());
    }

    public StoplightController(Stoplight stoplight, URL jenkinsHighPriorityViewUrl, URL jenkinsMediumPriorityViewUrl, JenkinsConnector connector) {
        this.stoplight = stoplight;
        this.jenkinsHighPriorityViewUrl = jenkinsHighPriorityViewUrl;
        this.jenkinsMediumPriorityViewUrl = jenkinsMediumPriorityViewUrl;
        this.connector = connector;
    }

    /**
     * The main polling loop of this controller.  This does not run in a separate thread.
     */
    public void run() {
        run(Integer.MAX_VALUE);
    }

    public Stoplight getStoplight() {
        return stoplight;
    }

    /**
     * Runs the main polling loop for a specified number of times.
     */
    protected void run(int numberOfIterations) {
        ContinuousIntegrationProjectSet highPrioritySet = new JenkinsProjectSet(jenkinsHighPriorityViewUrl, connector);
        ContinuousIntegrationProjectSet mediumPrioritySet = new JenkinsProjectSet(jenkinsMediumPriorityViewUrl, connector);
        initializeRun();

        int successCount = 0;
        int iterationCount = 0;
        for (int i = 0; i < numberOfIterations; i++) {
            // Determine the new state of all three lights before setting them.
            LightState red = LightState.OFF;
            LightState yellow = LightState.OFF;
            LightState green = LightState.OFF;

            // First we talk to Jenkins to see what state each of our lights _should_ be at.
            try {
                ContinuousIntegrationProjectState highPriState = highPrioritySet.fetchState();
                // At Innotas we treat TEST_FAILURE and FAILURE the same.
                if (highPriState.getCompletionStatus().isFailure()) {
                    red = getLightStateForRunningState(highPriState.getRunningStatus());
                }
                else if (highPriState.getCompletionStatus() != CompletionStatus.DISABLED) { // disabled can be left OFF
                    green = getLightStateForRunningState(highPriState.getRunningStatus());
                }

                ContinuousIntegrationProjectState mediumPriState = mediumPrioritySet.fetchState();
                if (mediumPriState.getCompletionStatus().isFailure()) {
                    yellow = getLightStateForRunningState(mediumPriState.getRunningStatus());
                }
                else if (mediumPriState.getCompletionStatus() != CompletionStatus.DISABLED) { // disabled can be left OFF
                    LightState newGreen = getLightStateForRunningState(mediumPriState.getRunningStatus());
                    // We may have a flashing green state from the high priority merging with this green state.
                    if (newGreen.ordinal() > green.ordinal()) green = newGreen;
                }
                successCount++;
            }
            catch (IOException e) {
                // I/O errors here mean that we had some kind of network failure talking to Jenkins.  We can log that,
                // but we want our loop to keep going.  If we couldn't talk to Jenkins at all, that means we'll be
                // setting our lights off, which is a good indicator of the unknown status.
                LOGGER.warn("Error fetching Jenkins status: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                LOGGER.debug("Error fetching Jenkins status: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
            finally {
                iterationCount++;
                if (iterationCount % ITERATIONS_BETWEEN_ALIVE_INFO == 0) {
                    int successes = successCount;
                    successCount = 0;
                    LOGGER.info("Still polling Jenkins after " + iterationCount + " iterations, with " + successes + " successful polls out of the last " + ITERATIONS_BETWEEN_ALIVE_INFO + ".");
                }
            }

            // Next we change our stoplight to reflect the new state.
            boolean stoplightChanged = (stoplight.getRed() != red || stoplight.getYellow() != yellow || stoplight.getGreen() != green);
            stoplight.setRed(red);
            stoplight.setYellow(yellow);
            stoplight.setGreen(green);

            // And lastly we sleep for some interval before polling Jenkins again.  We generally want to sleep longer and
            // longer, up to a point, unless our state says that something could change soon.
            try {
                if (stoplightChanged || stoplight.isFastFlashing()) timeBetweenIterations = MINIMUM_TIME_BETWEEN_ITERATIONS;
                else timeBetweenIterations = Math.min(timeBetweenIterations + 1000L, MAXIMUM_TIME_BETWEEN_ITERATIONS);
                Thread.sleep(timeBetweenIterations);
            }
            catch (InterruptedException e) {
                break;
            }
        }
    }

    protected void initializeRun() {
        LOGGER.info("Starting Stoplight controller polling of two Jenkins views:");
        LOGGER.info("    High priority: " + jenkinsHighPriorityViewUrl);
        LOGGER.info("  Medium priority: " + jenkinsMediumPriorityViewUrl);
        System.err.println("Starting Stoplight controller at " + new Date() + ".");
        System.out.println("Starting Stoplight controller at " + new Date() + ".");
    }

    /**
     * Returns a state for the light--solid, flashing, or fast flashing--based on whether the current running job
     * is finished, not finished, or almost finished.
     */
    protected LightState getLightStateForRunningState(RunningStatus runningStatus) {
        if (runningStatus == RunningStatus.ALMOST_FINISHED) return LightState.FAST_FLASHING;
        else if (runningStatus == RunningStatus.NOT_FINISHED) return LightState.FLASHING;
        return LightState.SOLID;
    }
}
