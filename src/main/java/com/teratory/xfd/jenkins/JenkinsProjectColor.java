package com.teratory.xfd.jenkins;

/**
 * The color of a Jenkins project, as described by Jenkins.
 *
 * Jenkins uses blue/yellow/red, plus disabled and aborted.
 *
 * @author jstokes
 */
public enum JenkinsProjectColor {

    BLUE,
    BLUE_ANIME,
    DISABLED,
    DISABLED_ANIME,
    ABORTED,
    ABORTED_ANIME,
    YELLOW,
    YELLOW_ANIME,
    RED,
    RED_ANIME,
    ;

    /** Determines if the current color indicates a building job. */
    public boolean isBuilding() {
        return toString().contains("_ANIME");
    }

    /** Determines if the current color indicates a failing job. */
    public boolean isFailing() {
        return !isDisabled() && this != BLUE && this != BLUE_ANIME;
    }

    /** Determines if the current color indicates a disabled job. */
    public boolean isDisabled() {
        return this == DISABLED || this == DISABLED_ANIME;
    }
}
