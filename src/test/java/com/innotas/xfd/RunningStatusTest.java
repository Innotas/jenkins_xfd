package com.innotas.xfd;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jstokes
 */
public class RunningStatusTest {

    @Test
    public void testValueOf() throws Exception {
        // This is a silly test, but gets us to 100% test coverage.  What's a more meaningful way to exercise valueOf()?
        assertTrue(RunningStatus.valueOf("NOT_RUNNING") == RunningStatus.NOT_RUNNING);
        assertTrue(RunningStatus.valueOf("NOT_FINISHED") == RunningStatus.NOT_FINISHED);
        assertTrue(RunningStatus.valueOf("ALMOST_FINISHED") == RunningStatus.ALMOST_FINISHED);
    }
}