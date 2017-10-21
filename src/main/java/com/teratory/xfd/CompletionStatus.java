package com.teratory.xfd;

/**
 * The status of the last completed run of a job or set of jobs.
 *
 * @author jstokes
 */
public enum CompletionStatus {

    DISABLED,
    SUCCESS,
    TEST_FAILURE,
    FAILURE,
    ;

    /** Returns true if this state represents a failure of any kind. */
    public boolean isFailure() {
        return toString().contains("FAILURE");
    }
}
