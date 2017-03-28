package com.innotas.xfd.jenkins;

import com.innotas.xfd.CompletionStatus;
import com.innotas.xfd.ContinuousIntegrationProjectState;
import com.innotas.xfd.RunningStatus;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author jstokes
 */
public class JenkinsProjectSetTest {

    private static final String BASE_JENKINS_URL = JenkinsProjectTest.BASE_JENKINS_URL;

    private MockJenkinsConnector mockConnector;
    private JenkinsProjectSet testProjectSet = null;

    @Before
    public void setUp() throws Exception {
        mockConnector = new MockJenkinsConnector();
        testProjectSet = new JenkinsProjectSet(new URL(BASE_JENKINS_URL + "view/ViewName"), mockConnector);
    }

    @Test
    public void testFetchState() throws Exception {
        // First try a custom view, with the XML our Radiator plugin would give us.
        mockConnector.pushNextXmlResponse("<radiatorView _class='hudson.model.RadiatorView'>" +
                                              "<job _class='hudson.model.FreeStyleProject'>" +
                                                 "<name>BuildFeature</name>" +
                                                 "<url>" + BASE_JENKINS_URL + "job/BuildFeature/</url>" +
                                                 "<color>red_anime</color>" +
                                              "</job>" +
                                              "<job _class='hudson.model.FreeStyleProject'>" +
                                                 "<name>TestCucumberFast-remote</name>" +
                                                 "<url>" + BASE_JENKINS_URL + "job/TestCucumberFast-remote/</url>" +
                                                 "<color>blue</color>" +
                                              "</job>" +
                                              "<name>Priority-Medium</name>" +
                                              "<url>" + BASE_JENKINS_URL + "view/Priority-Medium/</url>" +
                                          "</radiatorView>");
        // The blue project above won't result in further queries, but the red_anime one will have two follow ups, one
        // to get the running job, and one to see if that running job is almost finished.  Let's mock that response too.
        mockConnector.pushNextXmlResponse("<project><name>BuildFeature</name><color>red_anime</color><url>" + BASE_JENKINS_URL + "job/BuildFeature/11972/</url></project>");
        mockConnector.pushNextXmlResponse("<job><building>true</building><duration>0</duration><estimatedDuration>1778866</estimatedDuration><timestamp>" + (new Date().getTime() - 10 * 60 * 1000) + "</timestamp></job>");
        ContinuousIntegrationProjectState state = testProjectSet.fetchState();
        assertEquals(CompletionStatus.FAILURE, state.getCompletionStatus());
        assertEquals(RunningStatus.NOT_FINISHED, state.getRunningStatus());
        mockConnector.reset();

        // Next try the XML that a normal Jenkins list view would give us.
        mockConnector.pushNextXmlResponse("<listView _class='hudson.model.ListView'>" +
                                                  "<job _class='hudson.model.FreeStyleProject'>" +
                                                  "<name>BuildFeature</name>" +
                                                  "<url>" + BASE_JENKINS_URL + "job/BuildFeature/</url>" +
                                                  "<color>red_anime</color>" +
                                                  "</job>" +
                                                  "<job _class='hudson.model.FreeStyleProject'>" +
                                                  "<name>BuildIntegration</name>" +
                                                  "<url>" + BASE_JENKINS_URL + "job/BuildIntegration/</url>" +
                                                  "<color>disabled</color>" +
                                                  "</job>" +
                                                  "<job _class='hudson.model.FreeStyleProject'>" +
                                                  "<name>DeployFeature</name>" +
                                                  "<url>" + BASE_JENKINS_URL + "job/DeployFeature/</url>" +
                                                  "<color>blue</color>" +
                                                  "</job>" +
                                                  "<job _class='hudson.model.FreeStyleProject'>" +
                                                  "<name>RunControlFeatureServer</name>" +
                                                  "<url>" + BASE_JENKINS_URL + "job/RunControlFeatureServer/</url>" +
                                                  "<color>blue</color>" +
                                                  "</job>" +
                                                  "<name>Dev</name>" +
                                                  "<url>" + BASE_JENKINS_URL + "view/Dev/</url>" +
                                                  "</listView>");
        // Again, the red_anime project will result in follow-up queries that we'll want to also mock.
        mockConnector.pushNextXmlResponse("<project><name>BuildFeature</name><color>red_anime</color><url>" + BASE_JENKINS_URL + "job/BuildFeature/11972/</url></project>");
        mockConnector.pushNextXmlResponse("<job><building>true</building><duration>0</duration><estimatedDuration>1778866</estimatedDuration><timestamp>" + (new Date().getTime() - 10 * 60 * 1000) + "</timestamp></job>");
        state = testProjectSet.fetchState();
        assertEquals(CompletionStatus.FAILURE, state.getCompletionStatus());
        assertEquals(RunningStatus.NOT_FINISHED, state.getRunningStatus());
        mockConnector.reset();
    }
}