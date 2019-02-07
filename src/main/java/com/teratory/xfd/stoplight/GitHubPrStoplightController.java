package com.teratory.xfd.stoplight;

import com.comsysto.buildlight.cleware.driver.TrafficLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

public class GitHubPrStoplightController extends BaseStoplightController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubPrStoplightController.class);

    /** The main method of this class. */
    public static void main(String[] args) throws IOException {
        TrafficLight driver = createTrafficLightDriver();
        try (Stoplight stoplight = new Stoplight(driver)) {
            GitHubPrStoplightController controller = new GitHubPrStoplightController(stoplight);
            controller.run();
        }
    }

    public GitHubPrStoplightController(Stoplight stoplight) {
        super(stoplight);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected StopLightState fetchStatus() throws FetchStateException {
        // TODO: Find a simple library for fetching JSON from the GitHub v4 API.
        System.err.println("GitHubPrStoplightController.fetchStatus()");
        return new StopLightState(LightState.SOLID, LightState.FLASHING, LightState.FAST_FLASHING);
    }

    @Override
    protected void initializeRun() {
        LOGGER.info("Starting Stoplight controller polling of GitHub.");
        System.err.println("Starting Stoplight controller at " + new Date() + ".");
        System.out.println("Starting Stoplight controller at " + new Date() + ".");

    }
}
