package com.consort.service;


import com.consort.response.entities.Artifact;
import com.consort.response.entities.Build;
import com.consort.service.entities.ArtifactContent;
import com.consort.util.StreamUtils;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;

public class JenkinsServiceTest {

    public static final String USER = "techuser";
    public static final String PASSWORD = "tech123$";
    public static final String JENKINS_PROJECT = "{" +
            "\"lastBuild\" : {\n" +
            "    \"_class\" : \"org.jenkinsci.plugins.workflow.job.WorkflowRun\",\n" +
            "    \"number\" : 123,\n" +
            "    \"url\" : \"https://jenkins.consort-it.de/job/test-service/123/\"\n" +
            "}, \"lastSuccessfulBuild\" : {\n" +
            "    \"_class\" : \"org.jenkinsci.plugins.workflow.job.WorkflowRun\",\n" +
            "    \"number\" : 123,\n" +
            "    \"url\" : \"https://jenkins.consort-it.de/job/test-service/123/\"\n" +
            "}}";

    public static final String JENKINS_BUILD = "{" +
            "\"artifacts\" : [ {\n" +
            "\"displayPath\" : \"file\"," +
            "\"fileName\" : \"file\"," +
            "\"relativePath\" : \"file\"" +
            "}]," +
            "\"result\" : \"SUCCESS\"," +
            "\"id\" : \"123\"," +
            "\"url\" : \"https://jenkins.consort-it.de/job/test-service/123/\"\n" +
            "}";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8088));

    @Before
    public void setUp(){
        wireMockRule.resetScenarios();
        stubFor(get(urlEqualTo("/job/test-service/api/json"))
                .withBasicAuth(USER, PASSWORD)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(JENKINS_PROJECT)));

        stubFor(get(urlEqualTo("/job/test-service/123/api/json"))
                .withBasicAuth(USER, PASSWORD)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(JENKINS_BUILD)));

        stubFor(get(urlEqualTo("/job/test-service/123/artifact/file"))
                .withBasicAuth(USER, PASSWORD)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("some text")));
    }

    @Test
    public void getLastBuild() {
        Build lastBuild = JenkinsService.getInstance().getLastBuild("test-service");

        assertEquals(123, lastBuild.getNumber());
        assertEquals("https://jenkins.consort-it.de/job/test-service/123/", lastBuild.getUrl());
        assertEquals("SUCCESS", lastBuild.getResult());
    }

    @Test
    public void getLastSuccessfulBuild() {

        Build lastBuild = JenkinsService.getInstance().getLastSuccessfulBuild("test-service");

        assertEquals(123, lastBuild.getNumber());
        assertEquals("https://jenkins.consort-it.de/job/test-service/123/", lastBuild.getUrl());
        assertEquals("SUCCESS", lastBuild.getResult());
    }

    @Test
    public void getArtifacts() {

        List<Artifact> artifacts = JenkinsService.getInstance().getArtifacts("test-service", 123);

        assertEquals(1, artifacts.size());
        assertEquals("file", artifacts.get(0).getFilename());
    }

    @Test
    public void getArtifact() throws IOException {

        ArtifactContent artifact = JenkinsService.getInstance().getArtifact("test-service", 123,"file" );

        assertEquals("text/plain", artifact.getContentType());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        StreamUtils.copy(artifact.getContentStream(), output);

        assertEquals("some text", output.toString("UTF-8"));
    }
}