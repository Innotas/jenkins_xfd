package com.teratory.xfd;

import java.io.IOException;

/**
 * A collection of projects in the continuous integration system, which can report status as a group.
 */
public interface ContinuousIntegrationProjectSet {

    /**
     * Returns the current state of the set of projects.
     * @return the state, never <code>null</code>.
     * @throws IOException if a problem occurs determining the state.
     */
    ContinuousIntegrationProjectState fetchState() throws IOException;

}
