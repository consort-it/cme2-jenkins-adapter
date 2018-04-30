package com.consort.service;

import com.consort.response.entities.Artifact;
import com.consort.response.entities.Build;
import com.consort.service.entities.*;
import com.consort.util.EnvironmentContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JenkinsService {

    public static final String JENKINS_URL_PROPERTY = "JENKINS_URL";
    public static final String JENKINS_USER_PROPERTY = "JENKINS_USER";
    public static final String JENKINS_TOKEN_PROPERTY = "JENKINS_PWD";
    public static final String PATH_JOB = "/job/";
    public static final String PATH_API_JSON = "/api/json";
    public static final String PATH_ARTIFACT = "/artifact/";
    private static JenkinsService instance = null;
    private static ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(JenkinsService.class);
    private JenkinsService() {
    }

    public static JenkinsService getInstance() {
        if (instance == null) {
            instance = new JenkinsService();
        }

        return instance;
    }

    private HttpURLConnection getConnection(String url) {
        String user = EnvironmentContext.getInstance().getenv(JENKINS_USER_PROPERTY);
        String password = EnvironmentContext.getInstance().getenv(JENKINS_TOKEN_PROPERTY);
        String basicAuth = null;
        try {
            basicAuth = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("UTF-8"));
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Authorization", "Basic " + basicAuth);
            return connection;
        } catch (IOException e) {
            LOG.warn("Error connecting to jenkins url {}", url, e);
            throw new JenkinsException("Failed to create connection to Jenkins.");
        }
    }

    public Build getLastBuild(final String microservice) {
        LOG.debug("Getting last build for service {}", microservice);
        Validate.notNull(microservice);

        JenkinsProject project = getProject(microservice);

        if(project != null){

            return new Build(getBuild(microservice, project.getLastBuild().getNumber()));
        }

        return null;
    }

    public Build getLastSuccessfulBuild(String microservice) {
        LOG.debug("Getting last successful build for service {}", microservice);
        Validate.notNull(microservice);

        JenkinsProject project = getProject(microservice);

        if(project != null){
            return new Build(getBuild(microservice, project.getLastBuild().getNumber()));
        }

        return null;
    }

    private JenkinsProject getProject(String microservice) {
        LOG.debug("Query jenkins project for service {}", microservice);
        Validate.notNull(microservice);

        try {
            String jenkinsUrl = EnvironmentContext.getInstance().getenv(JENKINS_URL_PROPERTY) + PATH_JOB + microservice + PATH_API_JSON;
            HttpURLConnection connection = getConnection(jenkinsUrl);
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            InputStream content = connection.getInputStream();
            return mapper.readValue(content, JenkinsProject.class);
        } catch(FileNotFoundException e){
            LOG.warn("Project {} not found.", microservice, e);

        } catch(IOException e) {
            LOG.warn("Error loading project {}.", microservice, e);
        }

        return null;
    }

    private JenkinsBuildDetails getBuild(String microservice, int build) {
        LOG.debug("Query jenkins build {} for service {}", build, microservice);
        Validate.notNull(microservice);

        try {
            String jenkinsUrl = EnvironmentContext.getInstance().getenv(JENKINS_URL_PROPERTY) + PATH_JOB + microservice + "/" + build + PATH_API_JSON;
            HttpURLConnection connection = getConnection(jenkinsUrl);
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            InputStream content = connection.getInputStream();
            return mapper.readValue(content, JenkinsBuildDetails.class);
        } catch(FileNotFoundException e){
            LOG.warn("Build {} not found.", build, e);
        } catch(IOException e) {
            LOG.warn("Error loading build {}.", build, e);
        }
        return null;

    }

    public List<Artifact> getArtifacts(String microservice, int build) {
        LOG.debug("Listing artifacts for build {} of service {}", build, microservice);
        Validate.notNull(microservice);
        Validate.notNull(build);

        JenkinsBuildDetails fullBuild = getBuild(microservice, build);


        List<JenkinsArtifact> artifacts = Collections.emptyList();
        if(fullBuild != null){
            artifacts = fullBuild.getArtifacts();
        }

        return artifacts.stream().map(artifact -> new Artifact(artifact)).collect(Collectors.toList());

    }

    public ArtifactContent getArtifact(String microservice, int build, String filename) {
        LOG.debug("Getting artifact {} for build {} of service {}", filename, build, microservice);
        Validate.notNull(microservice);
        Validate.notNull(build);

        try {
            String jenkinsUrl = EnvironmentContext.getInstance().getenv(JENKINS_URL_PROPERTY) + PATH_JOB + microservice + "/" + build + PATH_ARTIFACT + filename;
            HttpURLConnection connection = getConnection(jenkinsUrl);
            connection.setRequestMethod("GET");
            connection.getInputStream();
            connection.getContentType();

            return new ArtifactContent(connection.getInputStream(), connection.getContentType());

        } catch (ProtocolException e) {
            LOG.warn("Error loading file: {}", filename, e);
        } catch (IOException e) {
            LOG.warn("File not found: {}", filename, e);
        }

        return null;
    }
}
