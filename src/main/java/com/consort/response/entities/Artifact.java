package com.consort.response.entities;

import com.consort.service.entities.JenkinsArtifact;

public class Artifact {
    private String filename;

    public Artifact(JenkinsArtifact artifact) {
        this.filename = artifact.getFileName();
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
