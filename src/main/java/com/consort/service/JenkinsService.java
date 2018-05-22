package com.consort.service;

import com.consort.response.entities.Artifact;
import com.consort.response.entities.Build;
import com.consort.service.entities.*;
import com.consort.util.EnvironmentContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import sun.rmi.runtime.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JenkinsService {

    private static final String JENKINS_URL_PROPERTY = "JENKINS_URL";
    private static final String JENKINS_USER_PROPERTY = "JENKINS_USER";
    private static final String JENKINS_TOKEN_PROPERTY = "JENKINS_PWD";
    private static final String PATH_JOB = "/job/pipeline_";
    private static final String PATH_CRUMB = "/crumbIssuer/api/xml";
    private static final String PATH_CRUMB_ARG = "xpath=concat(//crumbRequestField,\":\",//crumb)";
    private static final String PATH_API_JSON = "/api/json";
    private static final String PATH_ARTIFACT = "/artifact/";
    private static final String PATH_BUILD_ACTION = "/build";
    private static final String PATH_BUILD_ACTION_ARG = "delay=0sec";
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
        String basicAuth;
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

    public boolean build(final String microservice) {
        LOG.info("Build");
        Validate.notNull(microservice);

        final URIBuilder hostNameCrumb;
        try {
            hostNameCrumb = new URIBuilder(EnvironmentContext.getInstance().getenv(JENKINS_URL_PROPERTY) + PATH_CRUMB);
            hostNameCrumb.setParameter(PATH_CRUMB_ARG.split("=")[0], PATH_CRUMB_ARG.split("=")[1]);
//            hostNameCrumbArg =  URLEncoder.encode(PATH_CRUMB_ARG, "UTF-8");
        } catch(URISyntaxException e) {
            LOG.error("Error when modifying url for jenkins crumb", e);
            return false;
        }

        final URIBuilder hostName;
        byte[] authorizationB64;
        try {
            hostName = new URIBuilder(EnvironmentContext.getInstance().getenv(JENKINS_URL_PROPERTY) + PATH_JOB + microservice + PATH_BUILD_ACTION);
            hostName.setParameter(PATH_BUILD_ACTION_ARG.split("=")[0], PATH_BUILD_ACTION_ARG.split("=")[1]);
        } catch(URISyntaxException e) {
            LOG.error("Error when modifying url for jenkins", e);
            return false;
        }

        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            //First get Crumb that is required to prevent cross site conflicts
            LOG.info("ENV"+EnvironmentContext.getInstance().getenv(JENKINS_USER_PROPERTY) + ":" + EnvironmentContext.getInstance().getenv(JENKINS_TOKEN_PROPERTY));
            HttpGet httpGet = new HttpGet(hostNameCrumb.build());
            httpGet.setHeader("Authorization", "Basic "+Base64.getEncoder().encodeToString((EnvironmentContext.getInstance().getenv(JENKINS_USER_PROPERTY) + ":" + EnvironmentContext.getInstance().getenv(JENKINS_TOKEN_PROPERTY)).getBytes()));
            org.apache.http.HttpResponse respCrumb = httpClient.execute(httpGet);

            HttpEntity entity = respCrumb.getEntity();
            final String crumb = EntityUtils.toString(entity, "UTF-8");
            LOG.info(crumb);
            if(crumb.split(":").length!=2)
                LOG.error("Error crumb is wrong "+crumb);


            // Second do actual build
            HttpPost httpPost = new HttpPost(hostName.build());
            httpPost.setHeader("Authorization", "Basic "+Base64.getEncoder().encodeToString((EnvironmentContext.getInstance().getenv(JENKINS_USER_PROPERTY) + ":" + EnvironmentContext.getInstance().getenv(JENKINS_TOKEN_PROPERTY)).getBytes()));
            httpPost.setHeader(crumb.split(":")[0], crumb.split(":")[1]);
            org.apache.http.HttpResponse resp = httpClient.execute(httpPost);

            LOG.info(hostName.build().toString());
            LOG.info(httpPost.getAllHeaders()[0].getName()+" = "+httpPost.getAllHeaders()[0].getValue());
            LOG.info(httpPost.getAllHeaders()[1].getName()+" = "+httpPost.getAllHeaders()[1].getValue());

            StatusLine status = resp.getStatusLine();
            switch(Integer.toString(status.getStatusCode()).charAt(0) ) {
                case '2':
                    return true;
                case '4':
                    LOG.error("Error Jenkins delivered 4**");
                    break;
                default:
                    LOG.error("Error Jenkins delivered internal error "+status.getStatusCode());
                    break;
            }
        } catch(URISyntaxException e) {
            LOG.error("Error when building url", e);
            return false;
        } catch (IOException e) {
            LOG.error("Error when triggering jenkins", e);
        }
        return true;
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
            LOG.info(jenkinsUrl);
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
        throw new RuntimeException();
    }

    public List<Artifact> getArtifacts(String microservice, int build) {
        LOG.debug("Listing artifacts for build {} of service {}", build, microservice);
        Validate.notNull(microservice);

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
