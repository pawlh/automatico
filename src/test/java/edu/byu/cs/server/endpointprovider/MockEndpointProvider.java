package edu.byu.cs.server.endpointprovider;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

/**
 * A mock implementation of EndpointProvider designed for use with
 * Mockito.spy() (not mock()). Because individual endpoint methods only run once at
 * Server startup, they aren't useful for verifying endpoint calls.
 * MockEndpointProvider provides a runHandler() method that can be used for
 * that purpose.
 */
public class MockEndpointProvider implements EndpointProvider {

    /**
     * An empty function that is run by every endpoint, designed for use in
     * Mockito.verify() to verify endpoint calls. The individual endpoint
     * methods only run once on Server startup, so they can't be used
     * for that purpose.
     *
     * @param endpointName the name of the Route that was called
     */
    public void runHandler(String endpointName) {}

    /**
     * An empty function that is run for each path parameter in each endpoint
     * that is called. It is designed for use with Mockito.verify() to verify
     * that endpoints are called with specific path parameters.
     *
     * @param endpointName the name of the Route that was called
     * @param paramName    the name of the parameter
     * @param paramValue   the value of the parameter
     */
    public void hasPathParam(String endpointName, String paramName, String paramValue) {}

    private Object extractRequestInfo(String endpointName, Request req, Response res) {
        Map<String, String> params = req.params();
        for (String paramName : params.keySet()) {
            String paramValue = params.get(paramName);
            this.hasPathParam(endpointName, paramName, paramValue);
        }

        this.runHandler(endpointName);

        return "{}";
    }

    @Override
    public Filter beforeAll() {
        return (req, res) -> extractRequestInfo("beforeAll", req, res);
    }

    @Override
    public Filter afterAll() {
        return (req, res) -> extractRequestInfo("afterAll", req, res);
    }

    @Override
    public Route defaultGet() {
        return (req, res) -> extractRequestInfo("defaultGet", req, res);
    }

    @Override
    public Route usersGet() {
        return (req, res) -> extractRequestInfo("usersGet", req, res);
    }

    @Override
    public Route testModeGet() {
        return (req, res) -> extractRequestInfo("testModeGet", req, res);
    }

    @Override
    public Route commitAnalyticsGet() {
        return (req, res) -> extractRequestInfo("commitAnalyticsGet", req, res);
    }

    @Override
    public Route honorCheckerZipGet() {
        return (req, res) -> extractRequestInfo("honorCheckerZipGet", req, res);
    }

    @Override
    public Route sectionsGet() {
        return (req, res) -> extractRequestInfo("sectionsGet", req, res);
    }

    @Override
    public Filter verifyAuthenticatedMiddleware() {
        return (req, res) -> extractRequestInfo("verifyAuthenticatedMiddleware", req, res);
    }

    @Override
    public Filter verifyAdminMiddleware() {
        return (req, res) -> extractRequestInfo("verifyAdminMiddleware", req, res);
    }

    @Override
    public Route meGet() {
        return (req, res) -> extractRequestInfo("meGet", req, res);
    }

    @Override
    public Route callbackGet() {
        return (req, res) -> extractRequestInfo("callbackGet", req, res);
    }

    @Override
    public Route loginGet() {
        return (req, res) -> extractRequestInfo("loginGet", req, res);
    }

    @Override
    public Route logoutPost() {
        return (req, res) -> extractRequestInfo("logoutPost", req, res);
    }

    @Override
    public Route getConfigAdmin() {
        return (req, res) -> extractRequestInfo("getConfigAdmin", req, res);
    }

    @Override
    public Route getConfigStudent() {
        return (req, res) -> extractRequestInfo("getConfigStudent", req, res);
    }

    @Override
    public Route updateLivePhases() {
        return (req, res) -> extractRequestInfo("updateLivePhases", req, res);
    }

    @Override
    public Route scheduleShutdown() {
        return (req, res) -> extractRequestInfo("scheduleShutdown", req, res);
    }

    @Override
    public Route updateBannerMessage() {
        return (req, res) -> extractRequestInfo("updateBannerMessage", req, res);
    }

    @Override
    public Route updateCourseIdsPost() {
        return (req, res) -> extractRequestInfo("updateCourseIdsPost", req, res);
    }

    @Override
    public Route updateCourseIdsUsingCanvasGet() {
        return (req, res) -> extractRequestInfo("updateCourseIdsUsingCanvasGet", req, res);
    }

    @Override
    public Route updatePenalties() {
        return (req, res) -> extractRequestInfo("updatePenalties", req, res);
    }

    @Override
    public Route submitPost() {
        return (req, res) -> extractRequestInfo("submitPost", req, res);
    }

    @Override
    public Route adminRepoSubmitPost() {
        return (req, res) -> extractRequestInfo("adminRepoSubmitPost", req, res);
    }

    @Override
    public Route submitGet() {
        return (req, res) -> extractRequestInfo("submitGet", req, res);
    }

    @Override
    public Route latestSubmissionForMeGet() {
        return (req, res) -> extractRequestInfo("latestSubmissionForMeGet", req, res);
    }

    @Override
    public Route submissionXGet() {
        return (req, res) -> extractRequestInfo("submissionXGet", req, res);
    }

    @Override
    public Route latestSubmissionsGet() {
        return (req, res) -> extractRequestInfo("latestSubmissionsGet", req, res);
    }

    @Override
    public Route submissionsActiveGet() {
        return (req, res) -> extractRequestInfo("submissionsActiveGet", req, res);
    }

    @Override
    public Route studentSubmissionsGet() {
        return (req, res) -> extractRequestInfo("studentSubmissionsGet", req, res);
    }

    @Override
    public Route approveSubmissionPost() {
        return (req, res) -> extractRequestInfo("approveSubmissionPost", req, res);
    }

    @Override
    public Route submissionsReRunPost() {
        return (req, res) -> extractRequestInfo("submissionsReRunPost", req, res);
    }

    @Override
    public Route setRepoUrl() {
        return (req, res) -> extractRequestInfo("setRepoUrl", req, res);
    }

    @Override
    public Route setRepoUrlAdmin() {
        return (req, res) -> extractRequestInfo("setRepoUrlAdmin", req, res);
    }

    @Override
    public Route repoHistoryAdminGet() {
        return (req, res) -> extractRequestInfo("repoHistoryAdminGet", req, res);
    }
}
