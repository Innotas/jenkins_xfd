package com.teratory.xfd.stoplight;

import com.comsysto.buildlight.cleware.driver.TrafficLight;
import com.comsysto.buildlight.cleware.driver.TrafficLightFactory;
import com.teratory.xfd.stoplight.driver.ExternalProcessTrafficLight;
import com.teratory.xfd.stoplight.driver.PrintStreamMockTrafficLight;

import java.io.IOException;
import org.slf4j.Logger;

public abstract class BaseStoplightController {

    protected Stoplight stoplight;

    /** The number of polls we'll make before logging something to say we're still alive. */
    protected int iterationsBetweenAliveInfo = 10;

    /** How often we poll Jenkins for new state, in milliseconds. */
    protected long minimumTimeBetweenIterations = 1000L;

    /** The longest we'll wait, in milliseconds, between polls. */
    protected long maximumTimeBetweenIterations = 15000L;

    protected long timeBetweenIterations = minimumTimeBetweenIterations;

    public BaseStoplightController(Stoplight stoplight) {
        this.stoplight = stoplight;
    }

    /** Creates the driver class for the USB stoplight. */
    protected static TrafficLight createTrafficLightDriver() throws IOException {
        if ("true".equalsIgnoreCase(System.getProperty("consoleOnly"))) return new PrintStreamMockTrafficLight(System.out);
        else if (false) return TrafficLightFactory.createNewInstance();
        else return new ExternalProcessTrafficLight();
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

    protected abstract Logger getLogger();

    /**
     * Runs the main polling loop for a specified number of times.
     */
    protected void run(int numberOfIterations) {
        initializeRun();
        int successCount = 0;
        for (int i = 0; i < numberOfIterations; i++) {
            StopLightState newState;
            try {
                newState = fetchStatus();
                successCount++;
            }
            catch (FetchStateException fse) {
                Throwable cause = fse.getCause() != null ? fse.getCause() : fse;
                getLogger().warn("Error fetching status: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                getLogger().debug("Error fetching status: " + cause.getClass().getSimpleName() + ": " + cause.getMessage(), cause);
                newState = fse.getState();
            }
            if (i % this.iterationsBetweenAliveInfo == 0) {
                getLogger().info("Still polling after " + i + " iterations, with " + successCount + " successful polls out of the last " + this.iterationsBetweenAliveInfo + ".");
                successCount = 0;
            }

            // Next we change our stoplight to reflect the new state.
            boolean stoplightChanged = (stoplight.getRed() != newState.getRed() || stoplight.getYellow() != newState.getYellow() || stoplight.getGreen() != newState.getGreen());
            stoplight.setRed(newState.getRed());
            stoplight.setYellow(newState.getYellow());
            stoplight.setGreen(newState.getGreen());

            // And lastly we sleep for some interval before polling for status again.  We generally want to sleep longer and
            // longer, up to a point, unless our state says that something could change soon.
            try {
                sleepBetweenIterations(stoplightChanged);
            } catch (InterruptedException ie) {
                break; // stop this loop and end.
            }
        }
    }

    protected abstract StopLightState fetchStatus() throws FetchStateException;

    protected abstract void initializeRun();

    protected void sleepBetweenIterations(boolean stoplightJustChanged) throws InterruptedException {
        if (stoplightJustChanged || stoplight.isFastFlashing()) timeBetweenIterations = minimumTimeBetweenIterations;
        else timeBetweenIterations = Math.min(timeBetweenIterations + 1000L, maximumTimeBetweenIterations);
        Thread.sleep(timeBetweenIterations);
    }

    public static class FetchStateException extends Exception {

        private StopLightState state;

        public FetchStateException(StopLightState state, String message, Throwable cause) {
            super(message, cause);
            this.state = state;
        }

        public FetchStateException(StopLightState state, Throwable cause) {
            super(cause);
            this.state = state;
        }

        public StopLightState getState() {
            return state;
        }
    }
}
