package com.innotas.xfd.jenkins;

import com.innotas.xfd.CompletionStatus;
import com.innotas.xfd.ContinuousIntegrationProjectState;
import com.innotas.xfd.RunningStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * A representation of a project in Jenkins.
 *
 * A project has a completion status (of its last completed run) and a running status (of its current run, if any).
 *
 * @author jstokes
 */
public class JenkinsProject {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsProject.class);

    private URL projectUrl;
    private URL runningJobUrl;
    private JenkinsConnector connector;

    /**
     * Creates a project for the given URL.
     */
    public JenkinsProject(URL projectUrl) {
        this(projectUrl, new JenkinsConnector());
    }

    /**
     * Creates a project for the given URL.
     */
    protected JenkinsProject(URL projectUrl, JenkinsConnector connector) {
        this.projectUrl = projectUrl;
        this.connector = connector;
    }

    protected JenkinsConnector getConnector() {
        return connector;
    }

    /**
     * Retrieves the current state of this job.
     * @param color the current Jenkins color of the job.  Sometimes the state of the job can be determined entirely by
     *              the given color.
     */
    public ContinuousIntegrationProjectState fetchState(JenkinsProjectColor color) throws IOException {
        if (color.isBuilding()) {
            URL apiUrl = new URL(projectUrl, projectUrl.getPath() + (projectUrl.getPath().endsWith("/") ? "" : "/") + "api/xml?xpath=/*/lastBuild/url|/*/color|/*/name&wrapper=project");
            Element e = getConnector().loadJenkinsXml(apiUrl);
            String projectName = e.getElementsByTagName("name").item(0).getTextContent();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Detail status fetched for " + projectName + ".");
            }
            String runningJobUrl = e.getElementsByTagName("url").item(0).getTextContent();
            // We already know this Jenkins project is building right now, so we go ahead and query for the status of
            // that build to determine how much longer it has to go.
            boolean almostFinished = isAlmostFinished(new URL(runningJobUrl));
            return new ContinuousIntegrationProjectState(convertJenkinsColorToCompletionState(color), almostFinished ? RunningStatus.ALMOST_FINISHED : RunningStatus.NOT_FINISHED);
        }
        else {
            return new ContinuousIntegrationProjectState(convertJenkinsColorToCompletionState(color), RunningStatus.NOT_RUNNING);
        }
    }

    /**
     * Queries Jenkins to determine if the given job is almost finished.
     */
    protected boolean isAlmostFinished(URL jenkinsJobUrl) throws IOException {
        URL apiUrl = new URL(jenkinsJobUrl, jenkinsJobUrl.getPath() + "api/xml?xpath=/*/building|/*/timestamp|/*/duration|/*/estimatedDuration|/*/result&wrapper=job");
        Element e = getConnector().loadJenkinsXml(apiUrl);
        // Unfortunately duration is always 0 until the job finishes.
        long elapsedMillis;
        long duration = Long.parseLong(e.getElementsByTagName("duration").item(0).getTextContent());
        if (duration > 0) elapsedMillis = duration;
        else {
            long timestamp = Long.parseLong(e.getElementsByTagName("timestamp").item(0).getTextContent());
            Date jobStart = new Date(timestamp);
            Date now = new Date();
            elapsedMillis = now.getTime() - jobStart.getTime();
        }
        long estimatedDuration = Long.parseLong(e.getElementsByTagName("estimatedDuration").item(0).getTextContent());
        // If we have 5% or less to complete, we're almost finished.
        return (double)elapsedMillis / (double)estimatedDuration > 0.95;
    }

    /** Translates a Jenkins color value into a completion status value. */
    protected CompletionStatus convertJenkinsColorToCompletionState(JenkinsProjectColor color) {
        if (color == JenkinsProjectColor.BLUE || color == JenkinsProjectColor.BLUE_ANIME) return CompletionStatus.SUCCESS;
        else if (color.isDisabled()) return CompletionStatus.DISABLED;
        else if (color == JenkinsProjectColor.ABORTED || color == JenkinsProjectColor.ABORTED_ANIME) return CompletionStatus.TEST_FAILURE; // what else?
        else if (color == JenkinsProjectColor.YELLOW || color == JenkinsProjectColor.YELLOW_ANIME) return CompletionStatus.TEST_FAILURE;
        else if (color == JenkinsProjectColor.RED || color == JenkinsProjectColor.RED_ANIME) return CompletionStatus.FAILURE;
        else throw new IllegalArgumentException("Unknown Jenkins color: " + color);
    }
}
