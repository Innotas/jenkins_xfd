package com.innotas.xfd.stoplight;

import com.comsysto.buildlight.cleware.driver.Led;
import com.comsysto.buildlight.cleware.driver.TrafficLight;

/**
 * Defines the requirement for a TrafficLight implementation that can also answer questions about its current state.
 *
 * <p>
 *     No specific requirements are made on how the read method(s) are implemented.  Some implementations may remember
 *     which states they were last given, while others may read the real-time state from their device.
 * </p>
 * @author jstokes
 */
public interface ReadableTrafficLight extends TrafficLight {

    /**
     * Determines if the given LED is currently on.
     * @param led the LED to test, red, yellow, or green.
     * @return <code>true</code> if the LED is on, <code>false</code> if it is off.
     */
    boolean isLedOn(Led led);
}
