package com.teratory.xfd.stoplight;

import com.teratory.xfd.RunningStatus;
import com.teratory.xfd.jenkins.MockJenkinsConnector;
import com.teratory.xfd.stoplight.driver.PrintStreamMockTrafficLight;
import org.junit.Test;

import java.net.URL;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author jstokes
 */
public class StoplightControllerTest {

    private static final String BASE_JENKINS_URL = "http://nevercall.mockserver.com/jenkins/";

    private static final String HIGH_VIEW_URL = BASE_JENKINS_URL + "view/ViewName1/";
    private static final String MEDIUM_VIEW_URL = BASE_JENKINS_URL + "view/ViewName2/";


    @Test
    public void testGetLightStateForRunningState() throws Exception {
        StoplightController testController = new StoplightController(new Stoplight(new PrintStreamMockTrafficLight(System.out), false), new URL(HIGH_VIEW_URL), new URL(MEDIUM_VIEW_URL));
        assertEquals(LightState.SOLID, testController.getLightStateForRunningState(RunningStatus.NOT_RUNNING));
        assertEquals(LightState.FLASHING, testController.getLightStateForRunningState(RunningStatus.NOT_FINISHED));
        assertEquals(LightState.FAST_FLASHING, testController.getLightStateForRunningState(RunningStatus.ALMOST_FINISHED));
    }

    @Test
    public void testRunWithAllProjectsStandingGreen() throws Exception {
        MockJenkinsConnector mockConnector = new MockJenkinsConnector();
        StoplightController testController = new StoplightController(new Stoplight(new PrintStreamMockTrafficLight(System.out), false), new URL(HIGH_VIEW_URL), new URL(MEDIUM_VIEW_URL), mockConnector);

        mockConnector.pushNextXmlResponse("<radiatorView _class='hudson.model.RadiatorView'><job _class='hudson.model.FreeStyleProject'><name>BuildCandidate</name><url>" + BASE_JENKINS_URL + "job/BuildCandidate/</url><color>blue</color></job><job _class='hudson.model.FreeStyleProject'><name>BuildMaster</name><url>" + BASE_JENKINS_URL + "job/BuildMaster/</url><color>disabled</color></job><name>Priority-High</name><url>" + HIGH_VIEW_URL + "</url></radiatorView>\n");
        mockConnector.pushNextXmlResponse("<radiatorView _class='hudson.model.RadiatorView'><job _class='hudson.model.FreeStyleProject'><name>BuildFeature</name><url>" + BASE_JENKINS_URL + "job/BuildFeature/</url><color>blue</color></job><job _class='hudson.model.FreeStyleProject'><name>TestCucumberFast-remote</name><url>" + BASE_JENKINS_URL + "job/TestCucumberFast-remote/</url><color>blue</color></job><name>Priority-Medium</name><url>" + MEDIUM_VIEW_URL + "</url></radiatorView>\n");

        testController.run(1);

        assertFalse(testController.getStoplight().isFlashing());
        assertEquals(LightState.SOLID, testController.getStoplight().getGreen());
        assertEquals(LightState.OFF, testController.getStoplight().getYellow());
        assertEquals(LightState.OFF, testController.getStoplight().getRed());
    }

    @Test
    public void testRunWithAGreenProjectRunning() throws Exception {
        MockJenkinsConnector mockConnector = new MockJenkinsConnector();
        StoplightController testController = new StoplightController(new Stoplight(new PrintStreamMockTrafficLight(System.out), false), new URL(HIGH_VIEW_URL), new URL(MEDIUM_VIEW_URL), mockConnector);

        mockConnector.pushNextXmlResponse("<radiatorView _class='hudson.model.RadiatorView'><job _class='hudson.model.FreeStyleProject'><name>BuildCandidate</name><url>" + BASE_JENKINS_URL + "job/BuildCandidate/</url><color>blue</color></job><job _class='hudson.model.FreeStyleProject'><name>BuildMaster</name><url>" + BASE_JENKINS_URL + "job/BuildMaster/</url><color>disabled</color></job><name>Priority-High</name><url>" + HIGH_VIEW_URL + "</url></radiatorView>\n");
        mockConnector.pushNextXmlResponse("<radiatorView _class='hudson.model.RadiatorView'><job _class='hudson.model.FreeStyleProject'><name>BuildFeature</name><url>" + BASE_JENKINS_URL + "job/BuildFeature/</url><color>blue_anime</color></job><job _class='hudson.model.FreeStyleProject'><name>TestCucumberFast-remote</name><url>" + BASE_JENKINS_URL + "job/TestCucumberFast-remote/</url><color>blue</color></job><name>Priority-Medium</name><url>" + MEDIUM_VIEW_URL + "</url></radiatorView>\n");
        mockConnector.pushNextXmlResponse("<project><name>BuildFeature</name><color>blue_anime</color><url>" + BASE_JENKINS_URL + "job/BuildFeature/11968/</url></project>");
        mockConnector.pushNextXmlResponse("<job><building>true</building><duration>0</duration><estimatedDuration>1460044</estimatedDuration><timestamp>" + (new Date().getTime() - 10 * 60 * 1000) + "</timestamp></job>");

        testController.run(1);
        assertTrue(testController.getStoplight().isFlashing());
        assertFalse(testController.getStoplight().isFastFlashing());
        assertEquals(LightState.FLASHING, testController.getStoplight().getGreen());
        assertEquals(LightState.OFF, testController.getStoplight().getYellow());
        assertEquals(LightState.OFF, testController.getStoplight().getRed());
    }

    @Test
    public void testRunWithHighPriorityProjectFailing() throws Exception {
        MockJenkinsConnector mockConnector = new MockJenkinsConnector();
        StoplightController testController = new StoplightController(new Stoplight(new PrintStreamMockTrafficLight(System.out), false), new URL(HIGH_VIEW_URL), new URL(MEDIUM_VIEW_URL), mockConnector);

        mockConnector.pushNextXmlResponse("<radiatorView _class='hudson.model.RadiatorView'><job _class='hudson.model.FreeStyleProject'><name>BuildCandidate</name><url>" + BASE_JENKINS_URL + "job/BuildCandidate/</url><color>red</color></job><job _class='hudson.model.FreeStyleProject'><name>BuildMaster</name><url>" + BASE_JENKINS_URL + "job/BuildMaster/</url><color>disabled</color></job><name>Priority-High</name><url>" + HIGH_VIEW_URL + "</url></radiatorView>\n");
        mockConnector.pushNextXmlResponse("<radiatorView _class='hudson.model.RadiatorView'><job _class='hudson.model.FreeStyleProject'><name>BuildFeature</name><url>" + BASE_JENKINS_URL + "job/BuildFeature/</url><color>blue</color></job><job _class='hudson.model.FreeStyleProject'><name>TestCucumberFast-remote</name><url>" + BASE_JENKINS_URL + "job/TestCucumberFast-remote/</url><color>blue</color></job><name>Priority-Medium</name><url>" + MEDIUM_VIEW_URL + "</url></radiatorView>\n");

        testController.run(1);
        assertFalse(testController.getStoplight().isFlashing());
        assertFalse(testController.getStoplight().isFastFlashing());
        assertEquals(LightState.SOLID, testController.getStoplight().getGreen());
        assertEquals(LightState.OFF, testController.getStoplight().getYellow());
        assertEquals(LightState.SOLID, testController.getStoplight().getRed());
    }

    @Test
    public void testRunWithMediumPriorityProjectTestFailing() throws Exception {
        MockJenkinsConnector mockConnector = new MockJenkinsConnector();
        StoplightController testController = new StoplightController(new Stoplight(new PrintStreamMockTrafficLight(System.out), false), new URL(HIGH_VIEW_URL), new URL(MEDIUM_VIEW_URL), mockConnector);

        mockConnector.pushNextXmlResponse("<radiatorView _class='hudson.model.RadiatorView'><job _class='hudson.model.FreeStyleProject'><name>BuildCandidate</name><url>" + BASE_JENKINS_URL + "job/BuildCandidate/</url><color>blue</color></job><job _class='hudson.model.FreeStyleProject'><name>BuildMaster</name><url>" + BASE_JENKINS_URL + "job/BuildMaster/</url><color>disabled</color></job><name>Priority-High</name><url>" + HIGH_VIEW_URL + "</url></radiatorView>\n");
        mockConnector.pushNextXmlResponse("<radiatorView _class='hudson.model.RadiatorView'><job _class='hudson.model.FreeStyleProject'><name>BuildFeature</name><url>" + BASE_JENKINS_URL + "job/BuildFeature/</url><color>blue</color></job><job _class='hudson.model.FreeStyleProject'><name>TestCucumberFast-remote</name><url>" + BASE_JENKINS_URL + "job/TestCucumberFast-remote/</url><color>yellow</color></job><name>Priority-Medium</name><url>" + MEDIUM_VIEW_URL + "</url></radiatorView>\n");

        testController.run(1);
        assertFalse(testController.getStoplight().isFlashing());
        assertFalse(testController.getStoplight().isFastFlashing());
        assertEquals(LightState.SOLID, testController.getStoplight().getGreen());
        assertEquals(LightState.SOLID, testController.getStoplight().getYellow());
        assertEquals(LightState.OFF, testController.getStoplight().getRed());
    }
}