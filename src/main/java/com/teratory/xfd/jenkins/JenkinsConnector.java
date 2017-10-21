package com.teratory.xfd.jenkins;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Internal utility to fetch XML from a Jenkins endpoint.
 *
 * @author jstokes
 */
public class JenkinsConnector {

    /**
     * Fetches XML from a Jenkins endpoint.
     * @param url the URL.  If it does not end with <code>/api/xml</code>, that suffix will be appended.
     * @return the document element of the response, never <code>null</code>.
     * @throws IOException if something goes wrong.
     */
    public Element loadJenkinsXml(URL url) throws IOException {
        URL apiUrl;
        if (url.getPath().endsWith("/api/xml")) apiUrl = url;
        else apiUrl = new URL(url, url.getPath() + (url.getPath().endsWith("/") ? "" : "/") + "api/xml");
        return parseXml(apiUrl);
    }

    protected Element parseXml(URL url) throws IOException {
        URLConnection con = url.openConnection();
        con.setConnectTimeout(20000);
        con.setReadTimeout(20000);
        try (InputStream is = con.getInputStream()) {
            return parseXml(is);
        }
    }

    protected Element parseXml(InputStream is) throws IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            return doc.getDocumentElement();
        }
        catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
    }
}
