package com.innotas.xfd.jenkins;

import com.innotas.xfd.ContinuousIntegrationProjectSet;
import com.innotas.xfd.ContinuousIntegrationProjectState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URL;

/**
 * A ContinuousIntegrationProjectSet implemenation representing a Jenkins <i>view</i>, used to represent a collection of
 * projects with a single cumulative status.
 *
 * @author jstokes
 */
public class JenkinsProjectSet implements ContinuousIntegrationProjectSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsProjectSet.class);

    private URL jenkinsViewUrl;
    private JenkinsConnector connector;

    public JenkinsProjectSet(URL jenkinsViewUrl) {
        this(jenkinsViewUrl, new JenkinsConnector());
    }

    public JenkinsProjectSet(URL jenkinsViewUrl, JenkinsConnector connector) {
        this.jenkinsViewUrl = jenkinsViewUrl;
        this.connector = connector;
    }

    protected JenkinsConnector getConnector() {
        return connector;
    }

    @Override
    public ContinuousIntegrationProjectState fetchState() throws IOException {
        ContinuousIntegrationProjectState jenkinsViewState = null;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fetching status for " + jenkinsViewUrl + ".");
        }
        Element e = getConnector().loadJenkinsXml(jenkinsViewUrl);
        NodeList jobs = e.getElementsByTagName("job");
        for (int i = 0, len = jobs.getLength(); i < len; i++) {
            Element jobElement = (Element)jobs.item(i);
            String projectUrlString = jobElement.getElementsByTagName("url").item(0).getTextContent();
            String projectColorString = jobElement.getElementsByTagName("color").item(0).getTextContent();
            JenkinsProject project = new JenkinsProject(new URL(projectUrlString), getConnector());
            JenkinsProjectColor projectColor = JenkinsProjectColor.valueOf(projectColorString.toUpperCase());
            ContinuousIntegrationProjectState projectState = project.fetchState(projectColor);
            jenkinsViewState = (jenkinsViewState != null ? jenkinsViewState.accumulateState(projectState) : projectState);
        }
        return jenkinsViewState;
    }
}
