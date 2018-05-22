package com.consort.controller;

import com.consort.response.entities.Artifact;
import com.consort.response.entities.Build;
import com.consort.response.entities.ErrorResponse;
import com.consort.security.AuthorizationFilter;
import com.consort.service.JenkinsService;
import com.consort.service.entities.ArtifactContent;
import com.consort.util.StreamUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Service;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spark.Service.ignite;

public class JenkinsAdapterRouteController implements RouteController {

    public static final String API_PREFIX = "/api/v1/jenkins-adapter/";
    private static final String MICROSERVICE_BUILD_PATH = API_PREFIX + ":service/actions/:action";
    private static final String BUILDS_LAST_PATH = API_PREFIX + ":service/lastbuild";
    private static final String BUILDS_LAST_SUCCESSFUL_PATH = API_PREFIX + ":service/lastsuccessfulbuild";
    private static final String BUILDS_LIST_ARTIFACTS_PATH = API_PREFIX + ":service/artifacts";
    private static final String BUILDS_LIST_BUILD_ARTIFACTS_PATH = API_PREFIX + ":service/builds/:build/artifacts";
    private static final String BUILDS_GET_ARTIFACT_PATH = API_PREFIX + ":service/artifacts/:filename";
    private static final String BUILDS_GET_BUILD_ARTIFACT_PATH = API_PREFIX + ":service/builds/:build/artifacts/:filename";

    private static final String AUTHORIZER_NAME = "scope";
    private static final String SCOPE_READ = "jenkins-adapter/read";

    private static final ObjectMapper mapper = new ObjectMapper();
    public static final String PARAMETER_SERVICE = "service";
    public static final String BUILD_ACTION = "action";
    public static final String MESSAGE_NOT_FOUND = "Microservice or build not found.";
    public static final String ACTION_NOT_FOUND = "Action not found.";
    public static final String CODE_NOT_FOUND = "001";
    public static final String CODE_ACTION_NOT_FOUND = "003";
    public static final String MESSAGE_FILE_NOT_FOUND = "File not found.";
    public static final String CODE_FILE_NOT_FOUND = "002";

    public void initRoutes() {

        final Service http = ignite().port(8080);
        enableCORS(http, "*", "GET, POST", "Content-Type, Authorization");
        handlePostActionsMicroservice(http);
        handleGetLastBuild(http);
        handleGetLastSuccessfulBuild(http);
        handleListArtifacts(http);
        handleListBuildArtifacts(http);
        handleGetArtifact(http);
        handleGetBuildArtifact(http);
    }

    // Remove -v1 if available
    private String formMicroservice(String microService) throws Exception{
        Pattern p = Pattern.compile("(.*?(?=(-v1)|$))");
        Matcher m = p.matcher(microService);
        if (m.find()) {
            return m.group(1);
        }
        throw new Exception();
    }

    private void handlePostActionsMicroservice(final Service http) {

        http.before(MICROSERVICE_BUILD_PATH, new AuthorizationFilter(AUTHORIZER_NAME, SCOPE_READ));
        http.post(MICROSERVICE_BUILD_PATH, (req, res) -> {
            boolean build = false;
            if(req.params(BUILD_ACTION).equals("build"))
                build = JenkinsService.getInstance().build(formMicroservice(req.params(PARAMETER_SERVICE)));

            if (!build) {
                res.status(404);
                return mapper.writeValueAsString(new ErrorResponse(ACTION_NOT_FOUND, CODE_ACTION_NOT_FOUND));
            } else {
                res.status(200);
                return "";
            }
        });
    }

    private void handleGetLastBuild(final Service http) {

        http.before(BUILDS_LAST_PATH, new AuthorizationFilter(AUTHORIZER_NAME, SCOPE_READ));
        http.get(BUILDS_LAST_PATH, (req, res) -> {
            Build build = JenkinsService.getInstance().getLastBuild(formMicroservice(req.params(PARAMETER_SERVICE)));

            if (build == null) {
                res.status(404);
                return mapper.writeValueAsString(new ErrorResponse(MESSAGE_NOT_FOUND, CODE_NOT_FOUND));
            } else {
                res.status(200);
                return mapper.writeValueAsString(build);
            }
        });
    }

    private void handleGetLastSuccessfulBuild(final Service http) {

        http.before(BUILDS_LAST_SUCCESSFUL_PATH, new AuthorizationFilter(AUTHORIZER_NAME, SCOPE_READ));
        http.get(BUILDS_LAST_SUCCESSFUL_PATH, (req, res) -> {

            Build build = JenkinsService.getInstance().getLastSuccessfulBuild(formMicroservice(req.params(PARAMETER_SERVICE)));

            if (build == null) {
                res.status(404);
                return mapper.writeValueAsString(new ErrorResponse(MESSAGE_NOT_FOUND, CODE_NOT_FOUND));
            } else {
                res.status(200);
                return mapper.writeValueAsString(build);
            }
        });
    }

    private void handleListArtifacts(final Service http) {

        http.before(BUILDS_LIST_ARTIFACTS_PATH, new AuthorizationFilter(AUTHORIZER_NAME, SCOPE_READ));
        http.get(BUILDS_LIST_ARTIFACTS_PATH, (req, res) -> {
            Build build = JenkinsService.getInstance().getLastBuild(formMicroservice(req.params(PARAMETER_SERVICE)));
            if (build == null) {
                res.status(404);
                return mapper.writeValueAsString(new ErrorResponse(MESSAGE_NOT_FOUND, CODE_NOT_FOUND));
            }

            List<Artifact> artifacts = JenkinsService.getInstance().getArtifacts(formMicroservice(req.params(PARAMETER_SERVICE)), build.getNumber());
            res.status(200);
            return mapper.writeValueAsString(artifacts);
        });
    }

    private void handleListBuildArtifacts(final Service http) {

        http.before(BUILDS_LIST_BUILD_ARTIFACTS_PATH, new AuthorizationFilter(AUTHORIZER_NAME, SCOPE_READ));
        http.get(BUILDS_LIST_BUILD_ARTIFACTS_PATH, (req, res) -> {
            List<Artifact> artifacts = JenkinsService.getInstance().getArtifacts(formMicroservice(req.params(PARAMETER_SERVICE)), Integer.parseInt(req.params("build")));
            res.status(200);
            return mapper.writeValueAsString(artifacts);
        });
    }

    private void handleGetArtifact(final Service http) {

        http.before(BUILDS_GET_ARTIFACT_PATH, new AuthorizationFilter(AUTHORIZER_NAME, SCOPE_READ));
        http.get(BUILDS_GET_ARTIFACT_PATH, (req, res) -> {
            Build build = JenkinsService.getInstance().getLastBuild(formMicroservice(req.params(PARAMETER_SERVICE)));
            ArtifactContent artifact = JenkinsService.getInstance().getArtifact(formMicroservice(req.params(PARAMETER_SERVICE)), build.getNumber(), req.params("filename"));
            if(artifact != null)
            {
                res.status(200);
                res.header("Content-Type",artifact.getContentType());

                try(InputStream content = artifact.getContentStream()){
                    StreamUtils.copy(content, res.raw().getOutputStream());
                }
                return "";
            }
            else
            {
                res.status(404);
                return mapper.writeValueAsString(new ErrorResponse(MESSAGE_FILE_NOT_FOUND, CODE_FILE_NOT_FOUND));
            }
        });
    }

    private void handleGetBuildArtifact(final Service http) {

        http.before(BUILDS_GET_BUILD_ARTIFACT_PATH, new AuthorizationFilter(AUTHORIZER_NAME, SCOPE_READ));
        http.get(BUILDS_GET_BUILD_ARTIFACT_PATH, (req, res) -> {
            ArtifactContent artifact = JenkinsService.getInstance().getArtifact(formMicroservice(req.params(PARAMETER_SERVICE)), Integer.parseInt(req.params("build")), req.params("filename"));

            if(artifact != null)
            {
                res.status(200);
                res.header("Content-Type",artifact.getContentType());

                try(InputStream content = artifact.getContentStream()){
                    StreamUtils.copy(content, res.raw().getOutputStream());
                }
                return "";
            }
            else
            {
                res.status(404);
                return mapper.writeValueAsString(new ErrorResponse(MESSAGE_FILE_NOT_FOUND, CODE_FILE_NOT_FOUND));
            }



        });
    }


    private static void enableCORS(final Service http, final String origin, final String methods, final String headers) {

        http.options("/*", (req, res) -> {

            final String acRequestHeaders = req.headers("Access-Control-Request-Headers");
            if (acRequestHeaders != null) {
                res.header("Access-Control-Allow-Headers", acRequestHeaders);
            }

            final String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        http.before((req, res) -> {
            res.header("Access-Control-Allow-Origin", origin);
            res.header("Access-Control-Request-Method", methods);
            res.header("Access-Control-Allow-Headers", headers);
            res.type("application/json");
            res.header("Server", "-");
        });
    }
}
