package com.teratory.xfd.stoplight;

import com.comsysto.buildlight.cleware.driver.TrafficLight;
import com.teratory.xfd.github.GitHubConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

public class GitHubPrStoplightController extends BaseStoplightController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubPrStoplightController.class);

    private final GitHubConnector gitHubConnector;

    /** The main method of this class. */
    public static void main(String[] args) throws IOException {
        TrafficLight driver = createTrafficLightDriver();
        String apiKey = System.getProperty("github.api.key", null);
        if (apiKey == null) {
            LOGGER.error("Could not start up without 'github.api.key' JVM property.");
            System.exit(1);
        }
        try (Stoplight stoplight = new Stoplight(driver)) {
            GitHubConnector connector = new GitHubConnector(apiKey);
            GitHubPrStoplightController controller = new GitHubPrStoplightController(stoplight, connector);
            controller.run();
        }
        System.exit(0);
    }

    public GitHubPrStoplightController(Stoplight stoplight, GitHubConnector gitHubConnector) {
        super(stoplight);
        this.gitHubConnector = gitHubConnector;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected StopLightState fetchStatus() throws FetchStateException {

        LightState red = LightState.OFF;
        LightState yellow = LightState.OFF;
        LightState green = LightState.SOLID;

        try {
            int assignedToMeCount = gitHubConnector.getPullRequestsAssignedToMeCount();
            if (assignedToMeCount > 0) {
                red = assignedToMeCount > 1 ? LightState.FAST_FLASHING : LightState.FLASHING;
                green = LightState.OFF;
            }
        } catch (Exception e) {
            throw new FetchStateException(new StopLightState(red, yellow, green), e);
        }
        return new StopLightState(red, yellow, green);
    }

    @Override
    protected void initializeRun() {
        LOGGER.info("Starting Stoplight controller polling of GitHub.");
        System.err.println("Starting Stoplight controller at " + new Date() + ".");
        System.out.println("Starting Stoplight controller at " + new Date() + ".");

    }
}
