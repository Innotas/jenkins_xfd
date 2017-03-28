package com.innotas.xfd;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jstokes
 */
public class CompletionStatusTest {

    @Test
    public void testIsFailure() throws Exception {
        assertFalse(CompletionStatus.SUCCESS.isFailure());
        assertTrue(CompletionStatus.TEST_FAILURE.isFailure());
        assertTrue(CompletionStatus.FAILURE.isFailure());
        assertFalse(CompletionStatus.DISABLED.isFailure());
    }

    @Test
    public void testValueOf() {
        // This is a silly test, but gets us to 100% test coverage.  What's a more meaningful way to exercise valueOf()?
        assertTrue(CompletionStatus.valueOf("DISABLED") == CompletionStatus.DISABLED);
        assertTrue(CompletionStatus.valueOf("SUCCESS") == CompletionStatus.SUCCESS);
        assertTrue(CompletionStatus.valueOf("TEST_FAILURE") == CompletionStatus.TEST_FAILURE);
        assertTrue(CompletionStatus.valueOf("FAILURE") == CompletionStatus.FAILURE);
    }
}