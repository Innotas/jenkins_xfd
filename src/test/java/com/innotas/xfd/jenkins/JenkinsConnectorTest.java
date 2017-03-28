package com.innotas.xfd.jenkins;

import org.junit.Test;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * @author jstokes
 */
public class JenkinsConnectorTest {

    @Test
    public void testLoadJenkinsXml() throws Exception {
        final URL[] lastXmlUrl = {null};
        JenkinsConnector testConnector = new JenkinsConnector() {
            @Override
            protected Element parseXml(URL url) throws IOException {
                // Overridden to NOT try to open a stream from the given URL.
                lastXmlUrl[0] = url;
                return null;
            }
        };

        testConnector.loadJenkinsXml(new URL("http://some.server.com/some/path"));
        assertTrue(lastXmlUrl[0].toString().contains("api/xml"));
        testConnector.loadJenkinsXml(new URL("http://some.server.com/some/path/"));
        assertTrue(lastXmlUrl[0].toString().contains("api/xml"));
        testConnector.loadJenkinsXml(new URL("http://some.server.com/some/path/api/xml"));
        assertTrue(lastXmlUrl[0].toString().contains("api/xml"));
    }

    @Test
    public void testParseXml() throws Exception {
        JenkinsConnector testConnector = new JenkinsConnector();
        String xmlString = "<job><building>true</building><duration>0</duration><estimatedDuration>1460044</estimatedDuration></job>";
        Element job = testConnector.parseXml(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
        assertEquals("job", job.getTagName());
        assertEquals(3, job.getChildNodes().getLength());
        assertEquals("building", ((Element)job.getChildNodes().item(0)).getTagName());
        assertEquals("duration", ((Element)job.getChildNodes().item(1)).getTagName());
        assertEquals("estimatedDuration", ((Element)job.getChildNodes().item(2)).getTagName());
        assertEquals("1460044", job.getChildNodes().item(2).getTextContent());

        // I guess we can also test that it blows up right.
        xmlString = "<job><building>true</building><duration>0</duration><estimatedDuration>1460";
        try {
            testConnector.parseXml(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
            fail("Expected exception when parsing invalid XML.");
        }
        catch (IOException ignored) {
        }
    }
}