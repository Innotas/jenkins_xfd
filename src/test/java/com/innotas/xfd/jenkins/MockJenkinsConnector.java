package com.innotas.xfd.jenkins;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * A mock object used to simulate queries to Jenkins.
 *
 * @author jstokes
 */
public class MockJenkinsConnector extends JenkinsConnector {

    private String lastUrlQueried = null;
    private List<String> nextXmlResponses = new ArrayList<>();

    /** May be called by test code to confirm the URL last sent. */
    public String getLastUrlQueried() {
        return lastUrlQueried;
    }

    /** Should be called by test code to supply the XML for the next mock response. */
    public void pushNextXmlResponse(String nextXmlResponse) {
        nextXmlResponses.add(nextXmlResponse);
    }

    @Override
    public Element loadJenkinsXml(URL url) throws IOException {
        lastUrlQueried = url.toString();
        if (!nextXmlResponses.isEmpty()) {
            String nextXmlResponse = nextXmlResponses.remove(0);
            try (InputStream is = new ByteArrayInputStream(nextXmlResponse.getBytes(StandardCharsets.UTF_8));) {
                return parseXml(is);
            }
        }
        fail("No nextXmlResponse has been set, so a mock XML response cannot be given for the request " + url + ".");
        return null; // never reached.
    }

    public void reset() {
        reset(true);
    }

    public void reset(boolean failIfResponsesNotEmptied) {
        if (failIfResponsesNotEmptied && !nextXmlResponses.isEmpty()) {
            fail(nextXmlResponses.size() + " reponse(s) not read from mock connection.");
        }
    }
}
