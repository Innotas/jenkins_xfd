package com.innotas.xfd.stoplight;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.comsysto.buildlight.cleware.driver.Led;
import com.comsysto.buildlight.cleware.driver.TrafficLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A object representation of the physical stoplight sold by cleware.de.  This class surfaces three lights with various
 * on/off/flashing states through simple accessors, and internally handles the USB communication to the lights as well
 * as the animation used for flashing sequences.
 *
 * @author jstokes
 */
public class Stoplight implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Stoplight.class);

    private static final long FLASHING_ON_DURATION_IN_MILLIS = 5000;
    private static final long FLASHING_OFF_DURATION_IN_MILLIS = 1000;
    private static final long FAST_FLASHING_ON_DURATION_IN_MILLIS = 1500;
    private static final long FAST_FLASHING_OFF_DURATION_IN_MILLIS = 500;

    private LightState red = LightState.OFF;
    private LightState yellow = LightState.OFF;
    private LightState green = LightState.OFF;

    private ScheduledExecutorService scheduler = null;

    /** Internal states of a single color of LEDs. */
    protected enum LedState { OFF, ON; }

    /** Internal colors of LEDs. */
    protected enum LedColor { RED, YELLOW, GREEN; }

    private Map<LedColor,LedState> ledStatesByColor = new HashMap<>(5);
    private Map<LedColor,List<ScheduledFuture>> tasksByColor = new HashMap<>(5);

    private TrafficLight lightDriver;

    public Stoplight(TrafficLight lightDriver) {
        this(lightDriver, true);
    }

    public Stoplight(TrafficLight lightDriver, boolean animateStartup) {
        this.lightDriver = lightDriver;
        if (animateStartup) {
            for (int i = 0, len = 4; i < len; i++) {
                lightDriver.switchOnAllLeds();
                try { Thread.sleep(100L); } catch (InterruptedException ignored) {}
                lightDriver.switchOffAllLeds();
                try { Thread.sleep(100L); } catch (InterruptedException ignored) {}
            }
        }
        else {
            lightDriver.switchOnAllLeds();
            lightDriver.switchOffAllLeds();
        }
    }

    /** Returns the on/off/flashing state of the red LEDs. */
    public LightState getRed() {
        return red;
    }

    /** Returns the on/off/flashing state of the yellow LEDs. */
    public LightState getYellow() {
        return yellow;
    }

    /** Returns the on/off/flashing state of the green LEDs. */
    public LightState getGreen() {
        return green;
    }

    /** Sets the red LEDs into the given on/off/flashing state. */
    public void setRed(LightState newRed) {
        if (red != newRed) {
            LOGGER.debug("Changing red light to " + newRed + ".");
            red = newRed;
            startLedSequence(LedColor.RED, red);
        }
    }

    /** Sets the yellow LEDs into the given on/off/flashing state. */
    public void setYellow(LightState newYellow) {
        if (yellow != newYellow) {
            LOGGER.debug("Changing yellow light to " + newYellow + ".");
            yellow = newYellow;
            startLedSequence(LedColor.YELLOW, yellow);
        }
    }

    /** Sets the green LEDs into the given on/off/flashing state. */
    public void setGreen(LightState newGreen) {
        if (green != newGreen) {
            LOGGER.debug("Changing green light to " + newGreen + ".");
            green = newGreen;
            startLedSequence(LedColor.GREEN, green);
        }
    }

    /** Returns true if any light is in the FLASHING or FAST_FLASHING state. */
    public boolean isFlashing() {
        return getRed().isFlashing() || getYellow().isFlashing() || getGreen().isFlashing();
    }

    /** Returns true if any light is in the FAST_FLASHING state. */
    public boolean isFastFlashing() {
        return getRed() == LightState.FAST_FLASHING || getYellow() == LightState.FAST_FLASHING || getGreen() == LightState.FAST_FLASHING;
    }

    /** Returns the scheduler used to sequence animation tasks when flashing the LEDs of the stoplight. */
    protected ScheduledExecutorService getScheduler() {
        // Because the tasks themselves (sending OFF and ON signals to the stoplight) should be fast, a single
        // background thread for executing the tasks should be plenty.
        if (scheduler == null) scheduler = Executors.newSingleThreadScheduledExecutor();
        return scheduler;
    }

    private void startLedSequence(LedColor color, LightState state) {
        cancelLedSequence(color);
        if (state == LightState.OFF) setLed(color, LedState.OFF);
        else if (state == LightState.SOLID) setLed(color, LedState.ON);
        else {
            long onDuration = FLASHING_ON_DURATION_IN_MILLIS;
            long offDuration = FLASHING_OFF_DURATION_IN_MILLIS;
            if (state == LightState.FAST_FLASHING) {
                onDuration = FAST_FLASHING_ON_DURATION_IN_MILLIS;
                offDuration = FAST_FLASHING_OFF_DURATION_IN_MILLIS;
            }
            ScheduledFuture onTask = getScheduler().scheduleAtFixedRate(new SetLedTask(color, LedState.ON), 0, onDuration + offDuration, TimeUnit.MILLISECONDS);
            ScheduledFuture offTask = getScheduler().scheduleAtFixedRate(new SetLedTask(color, LedState.OFF), onDuration, onDuration + offDuration, TimeUnit.MILLISECONDS);
            storeLedSequence(color, onTask, offTask);
        }
    }

    /** Instantly turns the actual stoplight LEDs on or off for a specific color. */
    private void setLed(LedColor color, LedState onOrOff) {
        // Switch the light itself
        Led internalLed = Led.valueOf(color.name());
        if (onOrOff == LedState.ON) lightDriver.switchOn(internalLed);
        else lightDriver.switchOff(internalLed);
        LOGGER.debug("Turned " + color + " " + onOrOff + ".");
        ledStatesByColor.put(color, onOrOff);
    }

    /** Caches handles to running tasks for the given color, so they can be cancelled when required. */
    private void storeLedSequence(LedColor color, ScheduledFuture... tasks) {
        tasksByColor.put(color, Arrays.asList(tasks));
    }

    /** Cancels any background tasks related to animating the LEDs for the given color. */
    private void cancelLedSequence(LedColor color) {
        List<ScheduledFuture> runningTasks = tasksByColor.get(color);
        if (runningTasks != null) {
            for (ScheduledFuture task : runningTasks) {
                task.cancel(false);
            }
            tasksByColor.put(color, null);
        }
    }

    /** A repeatable task that simply turns the LEDs for a specific color on or off. */
    protected class SetLedTask implements Runnable {
        private LedColor color;
        private LedState state;

        public SetLedTask(LedColor color, LedState state) {
            this.color = color;
            this.state = state;
        }

        @Override
        public void run() {
            setLed(color, state);
        }
    }

    /** Closes this stoplight and related resources. */
    @Override
    public void close() {
        setGreen(LightState.OFF);
        setYellow(LightState.OFF);
        setRed(LightState.OFF);
        if (scheduler != null) {
            scheduler.shutdown();
            try { scheduler.awaitTermination(30, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
        }
        lightDriver.close();
    }
}
