package com.teratory.xfd.stoplight;

import com.comsysto.buildlight.cleware.driver.Led;
import com.teratory.xfd.stoplight.driver.PrintStreamMockTrafficLight;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jstokes
 */
public class StoplightTest {

    @Test
    public void testGettersAndSetters() throws Exception {
        Stoplight testStoplight = new Stoplight(new PrintStreamMockTrafficLight(System.out));

        assertEquals(LightState.OFF, testStoplight.getGreen());
        assertEquals(LightState.OFF, testStoplight.getYellow());
        assertEquals(LightState.OFF, testStoplight.getRed());
        testStoplight.setGreen(LightState.SOLID);
        assertEquals(LightState.SOLID, testStoplight.getGreen());
        assertEquals(LightState.OFF, testStoplight.getYellow());
        assertEquals(LightState.OFF, testStoplight.getRed());
        testStoplight.setYellow(LightState.FLASHING);
        assertEquals(LightState.SOLID, testStoplight.getGreen());
        assertEquals(LightState.FLASHING, testStoplight.getYellow());
        assertEquals(LightState.OFF, testStoplight.getRed());
        testStoplight.setRed(LightState.FAST_FLASHING);
        assertEquals(LightState.SOLID, testStoplight.getGreen());
        assertEquals(LightState.FLASHING, testStoplight.getYellow());
        assertEquals(LightState.FAST_FLASHING, testStoplight.getRed());
        testStoplight.setGreen(LightState.FLASHING);
        assertEquals(LightState.FLASHING, testStoplight.getGreen());
        assertEquals(LightState.FLASHING, testStoplight.getYellow());
        assertEquals(LightState.FAST_FLASHING, testStoplight.getRed());
        testStoplight.setYellow(LightState.FAST_FLASHING);
        assertEquals(LightState.FLASHING, testStoplight.getGreen());
        assertEquals(LightState.FAST_FLASHING, testStoplight.getYellow());
        assertEquals(LightState.FAST_FLASHING, testStoplight.getRed());
        testStoplight.setRed(LightState.OFF);
        assertEquals(LightState.FLASHING, testStoplight.getGreen());
        assertEquals(LightState.FAST_FLASHING, testStoplight.getYellow());
        assertEquals(LightState.OFF, testStoplight.getRed());
        testStoplight.setGreen(LightState.FAST_FLASHING);
        assertEquals(LightState.FAST_FLASHING, testStoplight.getGreen());
        assertEquals(LightState.FAST_FLASHING, testStoplight.getYellow());
        assertEquals(LightState.OFF, testStoplight.getRed());
        testStoplight.setYellow(LightState.OFF);
        assertEquals(LightState.FAST_FLASHING, testStoplight.getGreen());
        assertEquals(LightState.OFF, testStoplight.getYellow());
        assertEquals(LightState.OFF, testStoplight.getRed());
        testStoplight.setGreen(LightState.OFF);
        assertEquals(LightState.OFF, testStoplight.getGreen());
        assertEquals(LightState.OFF, testStoplight.getYellow());
        assertEquals(LightState.OFF, testStoplight.getRed());

        testStoplight.close();
    }

    @Test
    public void testAnnimation() throws Exception {
        PrintStreamMockTrafficLight mockDriver = new PrintStreamMockTrafficLight(System.out);
        Stoplight testStoplight = new Stoplight(mockDriver);

        assertEquals(LightState.OFF, testStoplight.getGreen());
        assertEquals(LightState.OFF, testStoplight.getYellow());
        assertEquals(LightState.OFF, testStoplight.getRed());

        // Sample the light state for 10 seconds and make sure nothing comes on.
        assertFalse(redWasBothOnAndOff(mockDriver, 10));

        // When flashing, over a ten second period we should see it both on and off at some point.
        testStoplight.setRed(LightState.FLASHING);
        assertTrue(redWasBothOnAndOff(mockDriver, 10));

        testStoplight.setRed(LightState.FAST_FLASHING);
        assertTrue(redWasBothOnAndOff(mockDriver, 5));

        testStoplight.close();
    }

    /** Tests the stoplight over time to make sure states change. */
    private boolean redWasBothOnAndOff(ReadableTrafficLight driver, int secondsToTest) {
        System.err.println("Watching Stoplight state for up to " + secondsToTest + " seconds.");
        boolean wasOff = false;
        boolean wasOn = false;
        long endTime = System.currentTimeMillis() + secondsToTest * 1000L;
        do {
            if (driver.isLedOn(Led.RED)) wasOn = true;
            else wasOff = true;
            try { Thread.sleep(200); } catch (InterruptedException e) { break; }
        } while (System.currentTimeMillis() < endTime && (!wasOff || !wasOn));
        return wasOff && wasOn;
    }

    @Test
    public void testIsFlashing() throws Exception {
        Stoplight testStoplight = new Stoplight(new PrintStreamMockTrafficLight(System.out), false);

        assertFalse(testStoplight.isFlashing());
        testStoplight.setGreen(LightState.SOLID);
        assertFalse(testStoplight.isFlashing());
        testStoplight.setYellow(LightState.SOLID);
        assertFalse(testStoplight.isFlashing());
        testStoplight.setRed(LightState.FLASHING);
        assertTrue(testStoplight.isFlashing());
        testStoplight.setGreen(LightState.FLASHING);
        assertTrue(testStoplight.isFlashing());
        testStoplight.setRed(LightState.OFF);
        assertTrue(testStoplight.isFlashing());
        testStoplight.setGreen(LightState.SOLID);
        assertFalse(testStoplight.isFlashing());
        testStoplight.setYellow(LightState.FAST_FLASHING);
        assertTrue(testStoplight.isFlashing());

        testStoplight.close();
    }

    @Test
    public void testIsFastFlashing() throws Exception {
        Stoplight testStoplight = new Stoplight(new PrintStreamMockTrafficLight(System.out), false);

        assertFalse(testStoplight.isFastFlashing());
        testStoplight.setGreen(LightState.SOLID);
        assertFalse(testStoplight.isFastFlashing());
        testStoplight.setYellow(LightState.SOLID);
        assertFalse(testStoplight.isFastFlashing());
        testStoplight.setRed(LightState.FLASHING);
        assertFalse(testStoplight.isFastFlashing());
        testStoplight.setGreen(LightState.FLASHING);
        assertFalse(testStoplight.isFastFlashing());
        testStoplight.setRed(LightState.OFF);
        assertFalse(testStoplight.isFastFlashing());
        testStoplight.setGreen(LightState.SOLID);
        assertFalse(testStoplight.isFastFlashing());

        testStoplight.setYellow(LightState.FAST_FLASHING);
        assertTrue(testStoplight.isFastFlashing());
        testStoplight.setYellow(LightState.FLASHING);
        assertFalse(testStoplight.isFastFlashing());
        testStoplight.setRed(LightState.FAST_FLASHING);
        assertTrue(testStoplight.isFastFlashing());
        testStoplight.setGreen(LightState.FAST_FLASHING);
        assertTrue(testStoplight.isFastFlashing());
        testStoplight.setRed(LightState.FLASHING);
        assertTrue(testStoplight.isFastFlashing());
        testStoplight.setGreen(LightState.FLASHING);
        assertFalse(testStoplight.isFastFlashing());

        testStoplight.close();
    }

    @Test
    public void testClose() {
        Stoplight testStoplight = new Stoplight(new PrintStreamMockTrafficLight(System.out), false);
        // closing the stoplight without ever having done anything to trigger teh animation scheduler should be fine.
        testStoplight.close();
    }
}