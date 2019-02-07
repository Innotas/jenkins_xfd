package com.teratory.xfd.stoplight;

/** An immutable data object representing the state of all three lights on a stoplight. */
public class StopLightState {

    private LightState red;
    private LightState yellow;
    private LightState green;

    public StopLightState(LightState red, LightState yellow, LightState green) {
        this.red = red;
        this.yellow = yellow;
        this.green = green;
        if (this.red == null) throw new NullPointerException("A red state is required.");
        if (this.yellow == null) throw new NullPointerException("A yellow state is required.");
        if (this.green == null) throw new NullPointerException("A green state is required.");
    }

    public LightState getRed() {
        return red;
    }

    public LightState getYellow() {
        return yellow;
    }

    public LightState getGreen() {
        return green;
    }
}
