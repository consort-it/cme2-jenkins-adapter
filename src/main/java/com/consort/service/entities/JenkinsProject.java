package com.consort.service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JenkinsProject {
    public JenkinsBuild getLastBuild() {
        return lastBuild;
    }

    private JenkinsBuild lastBuild;
    private JenkinsBuild lastSuccessfulBuild;

    public void setLastBuild(JenkinsBuild lastBuild) {
        this.lastBuild = lastBuild;
    }


    public JenkinsBuild getLastSuccessfulBuild() {
        return lastSuccessfulBuild;
    }

    public void setLastSuccessfulBuild(JenkinsBuild lastSuccessfulBuild) {
        this.lastSuccessfulBuild = lastSuccessfulBuild;
    }
}
