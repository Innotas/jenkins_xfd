package com.teratory.xfd;

/**
 * A data object representing the state of a project or set of projects, namely the completion and running states.
 *
 * @author jstokes
 */
public class ContinuousIntegrationProjectState {

    private CompletionStatus completionStatus;
    private RunningStatus runningStatus;

    public ContinuousIntegrationProjectState(CompletionStatus completionStatus, RunningStatus runningStatus) {
        super();
        this.completionStatus = completionStatus;
        this.runningStatus = runningStatus;
    }

    public CompletionStatus getCompletionStatus() {
        return completionStatus;
    }

    public RunningStatus getRunningStatus() {
        return runningStatus;
    }

    /** Returns a combination of this state and a given one. */
    public ContinuousIntegrationProjectState accumulateState(ContinuousIntegrationProjectState otherState) {
        // If one job is red and the other green, and the red job is sitting and the green job is running,
        // we don't want the running status of the green job to make the red light blink, because this would imply that
        // the job is red but something has been done about it.
        CompletionStatus cs;
        RunningStatus rs;
        if (getCompletionStatus().ordinal() < otherState.getCompletionStatus().ordinal()) {
            // The other status is greater or equal to this one.
            cs = otherState.getCompletionStatus();
            rs = otherState.getRunningStatus();
        }
        else if (getCompletionStatus() == otherState.getCompletionStatus()) {
            // the two are equal.
            cs = getCompletionStatus();
            rs = getRunningStatus().ordinal() >= otherState.getRunningStatus().ordinal() ? getRunningStatus() : otherState.getRunningStatus();
        }
        else {
            // The other status is less that this one.
            cs = getCompletionStatus();
            rs = getRunningStatus();
        }
        // As a special-case, if either running state is almost finished, that's our result.
        if (getRunningStatus() == RunningStatus.ALMOST_FINISHED || otherState.getRunningStatus() == RunningStatus.ALMOST_FINISHED) {
            rs = RunningStatus.ALMOST_FINISHED;
        }
        if (cs != getCompletionStatus() || rs != getRunningStatus()) {
            return new ContinuousIntegrationProjectState(cs, rs);
        }
        else return this;
    }
}
