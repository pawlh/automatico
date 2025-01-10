package edu.byu.cs.server.endpointprovider;

import edu.byu.cs.controller.*;
import edu.byu.cs.properties.ApplicationProperties;
import spark.Filter;
import spark.Route;

public class EndpointProviderImpl implements EndpointProvider {

    // Wildcard endpoints

    @Override
    public Filter beforeAll() {
        return (request, response) -> {
            response.header("Access-Control-Allow-Headers", "Authorization,Content-Type");
            response.header("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Credentials", "true");
            response.header("Access-Control-Allow-Origin", ApplicationProperties.frontendUrl());
        };
    }

    @Override
    public Filter afterAll() {
        return (req, res) -> {};
    }

    @Override
    public Route defaultGet() {
        return (req, res) -> {
            if (req.pathInfo().equals("/ws"))
                return null;

            String urlParams = req.queryString();
            urlParams = urlParams == null ? "" : "?" + urlParams;
            res.redirect("/" + urlParams, 302);
            return null;
        };
    }

    // AdminController

    @Override
    public Route usersGet() {
        return AdminController.usersGet;
    }

    @Override
    public Route testModeGet() {
        return AdminController.testModeGet;
    }

    @Override
    public Route commitAnalyticsGet() {
        return AdminController.commitAnalyticsGet;
    }

    @Override
    public Route honorCheckerZipGet() {
        return AdminController.honorCheckerZipGet;
    }

    @Override
    public Route sectionsGet() {
        return AdminController.sectionsGet;
    }

    // AuthController

    @Override
    public Filter verifyAuthenticatedMiddleware() {
        return AuthController.verifyAuthenticatedMiddleware;
    }

    @Override
    public Filter verifyAdminMiddleware() {
        return AuthController.verifyAdminMiddleware;
    }

    @Override
    public Route meGet() {
        return AuthController.meGet;
    }

    // CasController

    @Override
    public Route callbackGet() {
        return CasController.callbackGet;
    }

    @Override
    public Route loginGet() {
        return CasController.loginGet;
    }

    @Override
    public Route logoutPost() {
        return CasController.logoutPost;
    }

    // ConfigController

    @Override
    public Route getConfigAdmin() {
        return ConfigController.getConfigAdmin;
    }

    @Override
    public Route getConfigStudent() {
        return ConfigController.getConfigStudent;
    }

    @Override
    public Route updateLivePhases() {
        return ConfigController.updateLivePhases;
    }

    @Override
    public Route scheduleShutdown() {
        return ConfigController.scheduleShutdown;
    }

    @Override
    public Route updateBannerMessage() {
        return ConfigController.updateBannerMessage;
    }

    @Override
    public Route updateCourseIdsPost() {
        return ConfigController.updateCourseIdsPost;
    }

    @Override
    public Route updateCourseIdsUsingCanvasGet() {
        return ConfigController.updateCourseIdsUsingCanvasGet;
    }

    @Override
    public Route updatePenalties() {
        return ConfigController.updatePenalties;
    }

    // SubmissionController

    @Override
    public Route submitPost() {
        return SubmissionController.submitPost;
    }

    @Override
    public Route adminRepoSubmitPost() {
        return SubmissionController.adminRepoSubmitPost;
    }

    @Override
    public Route submitGet() {
        return SubmissionController.submitGet;
    }

    @Override
    public Route latestSubmissionForMeGet() {
        return SubmissionController.latestSubmissionForMeGet;
    }

    @Override
    public Route submissionXGet() {
        return SubmissionController.submissionXGet;
    }

    @Override
    public Route latestSubmissionsGet() {
        return SubmissionController.latestSubmissionsGet;
    }

    @Override
    public Route submissionsActiveGet() {
        return SubmissionController.submissionsActiveGet;
    }

    @Override
    public Route studentSubmissionsGet() {
        return SubmissionController.studentSubmissionsGet;
    }

    @Override
    public Route approveSubmissionPost() {
        return SubmissionController.approveSubmissionPost;
    }

    @Override
    public Route submissionsReRunPost() {
        return SubmissionController.submissionsReRunPost;
    }

    // UserController

    @Override
    public Route setRepoUrl() {
        return UserController.setRepoUrl;
    }

    @Override
    public Route setRepoUrlAdmin() {
        return UserController.setRepoUrlAdmin;
    }

    @Override
    public Route repoHistoryAdminGet() {
        return UserController.repoHistoryAdminGet;
    }
}
