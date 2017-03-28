package com.innotas.xfd.stoplight;

/**
 * The possibly-annimated state of a single light on the stoplight.
 *
 * @author jstokes
 */
public enum LightState {

    OFF,
    SOLID,
    FLASHING,
    FAST_FLASHING,
    ;

    /** Returns true if this state is flashing at all. */
    public boolean isFlashing() {
        return toString().contains("FLASHING");
    }
}
