package com.teratory.xfd.stoplight.driver;

import com.comsysto.buildlight.cleware.driver.Led;
import com.teratory.xfd.stoplight.ReadableTrafficLight;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A mock/test implementation of the Cleware TrafficLight that outputs the current state of all three lights to a
 * PrintStream, using backspace characters to re-write the stream to reflect the realtime state.
 *
 * @author jstokes
 */
public class PrintStreamMockTrafficLight implements ReadableTrafficLight {

    private static final String BACKSPACES = "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b";

    private final PrintStream out;
    private Map<Led, Boolean> currentState = new HashMap<>();
    private boolean initialized = false;

    public PrintStreamMockTrafficLight(PrintStream out) {
        this.out = out;
    }

    private void initialize() {
        if (!initialized) {
            out.println("");
            out.println(" RED   YELLOW  GREEN");
            out.print(" [ ]     [ ]    [ ]");
            initialized = true;
        }
    }
    @Override
    public void switchOn(Led led) {
        currentState.put(led, true);
        reprint();
    }

    @Override
    public void switchOff(Led led) {
        currentState.put(led, false);
        reprint();
    }

    @Override
    public void switchOnAllLeds() {
        currentState.put(Led.RED, true);
        currentState.put(Led.YELLOW, true);
        currentState.put(Led.GREEN, true);
        reprint();
    }

    @Override
    public void switchOffAllLeds() {
        currentState.clear();
        reprint();
    }

    /** Returns a map of the internal state of the lights, with a TRUE or FALSE value for each color. */
    private Map<Led,Boolean> getCurrentState() {
        return currentState;
    }

    @Override
    public boolean isLedOn(Led led) {
        return getCurrentState().get(led) == Boolean.TRUE;
    }

    private void reprint() {
        initialize();
        String r = " ";
        String y = " ";
        String g = " ";
        if (currentState.get(Led.RED) == Boolean.TRUE) r = "*";
        if (currentState.get(Led.YELLOW) == Boolean.TRUE) y = "*";
        if (currentState.get(Led.GREEN) == Boolean.TRUE) g = "*";
        System.out.print(BACKSPACES);
        System.out.print(" [" + r + "]     [" + y + "]    [" + g + "]");
    }

    @Override
    public void close() {
        System.out.print(BACKSPACES);
        System.out.println(" [c]     [c]    [c]");
    }
}
