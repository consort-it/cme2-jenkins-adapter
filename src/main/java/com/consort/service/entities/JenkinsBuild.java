package com.consort.service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JenkinsBuild {
    private int number;
    private String url;
    private List<JenkinsArtifact> artifacts;


    public List<JenkinsArtifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<JenkinsArtifact> artifacts) {
        this.artifacts = artifacts;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
