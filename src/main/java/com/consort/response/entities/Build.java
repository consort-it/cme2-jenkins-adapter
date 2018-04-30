package com.consort.response.entities;

import com.consort.service.entities.JenkinsBuildDetails;

public class Build {
    private int number;
    private String url;
    private String result;

    public Build(JenkinsBuildDetails build) {
        number = build.getId();
        url = build.getUrl();
        result = build.getResult();
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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
