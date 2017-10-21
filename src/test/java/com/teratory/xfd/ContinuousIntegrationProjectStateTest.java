package com.teratory.xfd;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jstokes
 */
public class ContinuousIntegrationProjectStateTest {

    @Test
    public void testAccumulateState() throws Exception {
        ContinuousIntegrationProjectState greenRunning = new ContinuousIntegrationProjectState(CompletionStatus.SUCCESS, RunningStatus.NOT_FINISHED);
        ContinuousIntegrationProjectState greenStopped = new ContinuousIntegrationProjectState(CompletionStatus.SUCCESS, RunningStatus.NOT_RUNNING);
        ContinuousIntegrationProjectState greenAlmostFinished = new ContinuousIntegrationProjectState(CompletionStatus.SUCCESS, RunningStatus.ALMOST_FINISHED);
        ContinuousIntegrationProjectState redRunning = new ContinuousIntegrationProjectState(CompletionStatus.FAILURE, RunningStatus.NOT_FINISHED);
        ContinuousIntegrationProjectState redStopped = new ContinuousIntegrationProjectState(CompletionStatus.FAILURE, RunningStatus.NOT_RUNNING);
        ContinuousIntegrationProjectState redAlmostFinished = new ContinuousIntegrationProjectState(CompletionStatus.FAILURE, RunningStatus.ALMOST_FINISHED);

        assertEquals(CompletionStatus.SUCCESS, greenRunning.accumulateState(greenAlmostFinished).getCompletionStatus());
        assertEquals(RunningStatus.ALMOST_FINISHED, greenRunning.accumulateState(greenAlmostFinished).getRunningStatus());
        assertEquals(CompletionStatus.SUCCESS, greenAlmostFinished.accumulateState(greenRunning).getCompletionStatus());
        assertEquals(RunningStatus.ALMOST_FINISHED, greenAlmostFinished.accumulateState(greenRunning).getRunningStatus());

        assertEquals(CompletionStatus.SUCCESS, greenRunning.accumulateState(greenStopped).getCompletionStatus());
        assertEquals(RunningStatus.NOT_FINISHED, greenRunning.accumulateState(greenStopped).getRunningStatus());
        assertEquals(CompletionStatus.SUCCESS, greenStopped.accumulateState(greenRunning).getCompletionStatus());
        assertEquals(RunningStatus.NOT_FINISHED, greenStopped.accumulateState(greenRunning).getRunningStatus());

        assertEquals(CompletionStatus.FAILURE, greenRunning.accumulateState(redStopped).getCompletionStatus());
        assertEquals(RunningStatus.NOT_RUNNING, greenRunning.accumulateState(redStopped).getRunningStatus());
        assertEquals(CompletionStatus.FAILURE, redStopped.accumulateState(greenRunning).getCompletionStatus());
        assertEquals(RunningStatus.NOT_RUNNING, redStopped.accumulateState(greenRunning).getRunningStatus());

        assertEquals(CompletionStatus.FAILURE, greenStopped.accumulateState(redRunning).getCompletionStatus());
        assertEquals(RunningStatus.NOT_FINISHED, greenStopped.accumulateState(redRunning).getRunningStatus());
        assertEquals(CompletionStatus.FAILURE, redRunning.accumulateState(greenStopped).getCompletionStatus());
        assertEquals(RunningStatus.NOT_FINISHED, redRunning.accumulateState(greenStopped).getRunningStatus());

        // As an exception, if either is almost-finished we flash.
        assertEquals(CompletionStatus.FAILURE, greenAlmostFinished.accumulateState(redStopped).getCompletionStatus());
        assertEquals(RunningStatus.ALMOST_FINISHED, greenAlmostFinished.accumulateState(redStopped).getRunningStatus());
        assertEquals(CompletionStatus.FAILURE, redStopped.accumulateState(greenAlmostFinished).getCompletionStatus());
        assertEquals(RunningStatus.ALMOST_FINISHED, redStopped.accumulateState(greenAlmostFinished).getRunningStatus());

        assertEquals(CompletionStatus.FAILURE, greenStopped.accumulateState(redAlmostFinished).getCompletionStatus());
        assertEquals(RunningStatus.ALMOST_FINISHED, greenStopped.accumulateState(redAlmostFinished).getRunningStatus());
        assertEquals(CompletionStatus.FAILURE, redAlmostFinished.accumulateState(greenStopped).getCompletionStatus());
        assertEquals(RunningStatus.ALMOST_FINISHED, redAlmostFinished.accumulateState(greenStopped).getRunningStatus());
    }
}