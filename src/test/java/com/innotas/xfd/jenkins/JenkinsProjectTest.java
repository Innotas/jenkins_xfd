package com.innotas.xfd.jenkins;

import com.innotas.xfd.CompletionStatus;
import com.innotas.xfd.ContinuousIntegrationProjectState;
import com.innotas.xfd.RunningStatus;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author jstokes
 */
public class JenkinsProjectTest {

    protected static final String BASE_JENKINS_URL = "http://nevercall.mockserver.com/jenkins/";

    private MockJenkinsConnector mockConnector;
    private JenkinsProject testProject = null;

    @Before
    public void setUp() throws Exception {
        mockConnector = new MockJenkinsConnector();
        testProject = new JenkinsProject(new URL(BASE_JENKINS_URL), mockConnector);
    }

    @Test
    public void testFetchState() throws Exception {
        // Because of the Jenkins color for these, they won't result in a query.
        ContinuousIntegrationProjectState state = testProject.fetchState(JenkinsProjectColor.BLUE);
        assertEquals(CompletionStatus.SUCCESS, state.getCompletionStatus());
        assertEquals(RunningStatus.NOT_RUNNING, state.getRunningStatus());

        state = testProject.fetchState(JenkinsProjectColor.YELLOW);
        assertEquals(CompletionStatus.TEST_FAILURE, state.getCompletionStatus());
        assertEquals(RunningStatus.NOT_RUNNING, state.getRunningStatus());

        state = testProject.fetchState(JenkinsProjectColor.RED);
        assertEquals(CompletionStatus.FAILURE, state.getCompletionStatus());
        assertEquals(RunningStatus.NOT_RUNNING, state.getRunningStatus());

        state = testProject.fetchState(JenkinsProjectColor.DISABLED);
        assertEquals(CompletionStatus.DISABLED, state.getCompletionStatus());
        assertEquals(RunningStatus.NOT_RUNNING, state.getRunningStatus());

        // Now test projects with running jobs that will result in queries to determine details.
        mockConnector.pushNextXmlResponse("<project><name>BuildFeature</name><color>blue_anime</color><url>" + BASE_JENKINS_URL + "job/BuildFeature/11968/</url></project>");
        mockConnector.pushNextXmlResponse("<job><building>true</building><duration>0</duration><estimatedDuration>1460044</estimatedDuration><timestamp>" + (new Date().getTime() - 10 * 60 * 1000) + "</timestamp></job>");
        state = testProject.fetchState(JenkinsProjectColor.BLUE_ANIME);
        assertEquals(CompletionStatus.SUCCESS, state.getCompletionStatus());
        assertEquals(RunningStatus.NOT_FINISHED, state.getRunningStatus());

        mockConnector.pushNextXmlResponse("<project><name>BuildFeature</name><color>red_anime</color><url>" + BASE_JENKINS_URL + "job/BuildFeature/11968/</url></project>");
        mockConnector.pushNextXmlResponse("<job><building>true</building><duration>0</duration><estimatedDuration>1460044</estimatedDuration><timestamp>" + (new Date().getTime() - 25*60*1000) + "</timestamp></job>");
        state = testProject.fetchState(JenkinsProjectColor.RED_ANIME);
        assertEquals(CompletionStatus.FAILURE, state.getCompletionStatus());
        assertEquals(RunningStatus.ALMOST_FINISHED, state.getRunningStatus());
    }

    @Test
    public void testIsAlmostFinished() throws Exception {
        URL mockJobUrl = new URL(BASE_JENKINS_URL + "/job/BuildCandidate/12345/");
        Calendar c = Calendar.getInstance();
        long startedJustNow = c.getTime().getTime();
        c.add(Calendar.MINUTE, -10);
        long startedTenMinutesAgo = c.getTime().getTime();
        c.add(Calendar.MINUTE, -10);
        long startedTwentyMinutesAgo = c.getTime().getTime();
        c.add(Calendar.MINUTE, -3);
        long startedTwentyThreeMinutesAgo = c.getTime().getTime();
        c.add(Calendar.MINUTE, -1);
        long startedTwentyFourMinutesAgo = c.getTime().getTime();
        c.add(Calendar.MINUTE, -1);
        long startedTwentyFiveMinutesAgo = c.getTime().getTime();

        // This job takes a little more than 24 minutes to complete.  We're always testing whether it's almost finished
        // based on _now_, so we'll try responses that say it _started_ at various times in the past.
        mockConnector.pushNextXmlResponse("<job><building>true</building><duration>0</duration><estimatedDuration>1460044</estimatedDuration><timestamp>" + startedJustNow + "</timestamp></job>");
        assertFalse(testProject.isAlmostFinished(mockJobUrl));
        mockConnector.pushNextXmlResponse("<job><building>true</building><duration>0</duration><estimatedDuration>1460044</estimatedDuration><timestamp>" + startedTenMinutesAgo + "</timestamp></job>");
        assertFalse(testProject.isAlmostFinished(mockJobUrl));
        mockConnector.pushNextXmlResponse("<job><building>true</building><duration>0</duration><estimatedDuration>1460044</estimatedDuration><timestamp>" + startedTwentyMinutesAgo + "</timestamp></job>");
        assertFalse(testProject.isAlmostFinished(mockJobUrl));
        mockConnector.pushNextXmlResponse("<job><building>true</building><duration>0</duration><estimatedDuration>1460044</estimatedDuration><timestamp>" + startedTwentyThreeMinutesAgo + "</timestamp></job>");
        assertFalse(testProject.isAlmostFinished(mockJobUrl));
        mockConnector.pushNextXmlResponse("<job><building>true</building><duration>0</duration><estimatedDuration>1460044</estimatedDuration><timestamp>" + startedTwentyFourMinutesAgo + "</timestamp></job>");
        assertTrue(testProject.isAlmostFinished(mockJobUrl));
        mockConnector.pushNextXmlResponse("<job><building>true</building><duration>0</duration><estimatedDuration>1460044</estimatedDuration><timestamp>" + startedTwentyFiveMinutesAgo + "</timestamp></job>");
        assertTrue(testProject.isAlmostFinished(mockJobUrl));
    }

    @Test
    public void testConvertJenkinsColorToCompletionState() throws Exception {
        assertEquals(CompletionStatus.SUCCESS, testProject.convertJenkinsColorToCompletionState(JenkinsProjectColor.BLUE));
        assertEquals(CompletionStatus.SUCCESS, testProject.convertJenkinsColorToCompletionState(JenkinsProjectColor.BLUE_ANIME));
        assertEquals(CompletionStatus.DISABLED, testProject.convertJenkinsColorToCompletionState(JenkinsProjectColor.DISABLED));
        assertEquals(CompletionStatus.TEST_FAILURE, testProject.convertJenkinsColorToCompletionState(JenkinsProjectColor.ABORTED));
        assertEquals(CompletionStatus.TEST_FAILURE, testProject.convertJenkinsColorToCompletionState(JenkinsProjectColor.ABORTED_ANIME));
        assertEquals(CompletionStatus.TEST_FAILURE, testProject.convertJenkinsColorToCompletionState(JenkinsProjectColor.YELLOW));
        assertEquals(CompletionStatus.TEST_FAILURE, testProject.convertJenkinsColorToCompletionState(JenkinsProjectColor.YELLOW_ANIME));
        assertEquals(CompletionStatus.FAILURE, testProject.convertJenkinsColorToCompletionState(JenkinsProjectColor.RED));
        assertEquals(CompletionStatus.FAILURE, testProject.convertJenkinsColorToCompletionState(JenkinsProjectColor.RED_ANIME));
    }

}