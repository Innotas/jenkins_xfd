package com.teratory.xfd.stoplight;

import com.comsysto.buildlight.cleware.driver.TrafficLight;
import com.teratory.xfd.CompletionStatus;
import com.teratory.xfd.ContinuousIntegrationProjectSet;
import com.teratory.xfd.ContinuousIntegrationProjectState;
import com.teratory.xfd.RunningStatus;
import com.teratory.xfd.jenkins.JenkinsConnector;
import com.teratory.xfd.jenkins.JenkinsProjectSet;
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
public class JenkinsStoplightController extends BaseStoplightController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsStoplightController.class);

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
            JenkinsStoplightController controller = new JenkinsStoplightController(stoplight, highPriorityJenkinsView, mediumPriorityJenkinsView);
            controller.run();
        }
    }

    private URL jenkinsHighPriorityViewUrl;
    private URL jenkinsMediumPriorityViewUrl;
    private JenkinsConnector connector;
    private ContinuousIntegrationProjectSet highPrioritySet;
    private ContinuousIntegrationProjectSet mediumPrioritySet;


    public JenkinsStoplightController(Stoplight stoplight, URL jenkinsHighPriorityViewUrl, URL jenkinsMediumPriorityViewUrl) {
        this(stoplight, jenkinsHighPriorityViewUrl, jenkinsMediumPriorityViewUrl, new JenkinsConnector());
    }

    public JenkinsStoplightController(Stoplight stoplight, URL jenkinsHighPriorityViewUrl, URL jenkinsMediumPriorityViewUrl, JenkinsConnector connector) {
        super(stoplight);
        this.jenkinsHighPriorityViewUrl = jenkinsHighPriorityViewUrl;
        this.jenkinsMediumPriorityViewUrl = jenkinsMediumPriorityViewUrl;
        this.connector = connector;
    }

    @Override
    protected StopLightState fetchStatus() throws FetchStateException {
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
            return new StopLightState(red, yellow, green);
        }
        catch (IOException e) {
            throw new FetchStateException(new StopLightState(red, yellow, green), e);
        }
    }

    @Override
    protected void initializeRun() {
        highPrioritySet = new JenkinsProjectSet(jenkinsHighPriorityViewUrl, connector);
        mediumPrioritySet = new JenkinsProjectSet(jenkinsMediumPriorityViewUrl, connector);

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

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
