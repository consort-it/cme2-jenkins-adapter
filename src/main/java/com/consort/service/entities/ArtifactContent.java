package com.consort.service.entities;

import java.io.InputStream;

public class ArtifactContent {

    private String contentType;
    private InputStream contentStream;

    public ArtifactContent(InputStream inputStream, String contentType) {
        this.contentType = contentType;
        this.contentStream =inputStream;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public void setContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
    }
}
