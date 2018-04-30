package com.consort.service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JenkinsBuildDetails {
    private int id;
    private String url;
    private List<JenkinsArtifact> artifacts;
    private String result;


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


    public List<JenkinsArtifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<JenkinsArtifact> artifacts) {
        this.artifacts = artifacts;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
