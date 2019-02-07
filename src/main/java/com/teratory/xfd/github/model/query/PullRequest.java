package com.teratory.xfd.github.model.query;

import java.net.MalformedURLException;
import java.net.URL;

public class PullRequest {

    private int number;
    private String title;
    private URL url;

    public PullRequest(int number, String title, String url) {
        this.number = number;
        this.title = title;
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public URL getUrl() {
        return url;
    }
}
