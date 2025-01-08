package edu.byu.cs.server.endpointprovider;

import edu.byu.cs.controller.*;
import edu.byu.cs.properties.ApplicationProperties;

import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

public class EndpointProviderImpl implements EndpointProvider {

    // Wildcard endpoints

    @Override
    public Handler beforeAll() {
        return ctx -> {
            ctx.header("Access-Control-Allow-Headers", "Authorization,Content-Type");
            ctx.header("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,PATCH,OPTIONS");
            ctx.header("Access-Control-Allow-Credentials", "true");
            ctx.header("Access-Control-Allow-Origin", ApplicationProperties.frontendUrl());
        };
    }

    @Override
    public Handler afterAll() {
        return ctx -> {};
    }

    @Override
    public Handler defaultGet() {
        return ctx -> {
            if (!ctx.path().equals("/ws")) {
                String urlParams = ctx.queryString();
                urlParams = urlParams == null ? "" : "?" + urlParams;
                ctx.redirect("/" + urlParams, HttpStatus.FOUND);
            }
        };
    }

    @Override
    public Handler defaultOptions() {
        return ctx -> {};
    }

    // AdminController

    @Override
    public Handler usersGet() {
        return AdminController.usersGet;
    }

    @Override
    public Handler testModeGet() {
        return AdminController.testModeGet;
    }

    @Override
    public Handler commitAnalyticsGet() {
        return AdminController.commitAnalyticsGet;
    }

    @Override
    public Handler honorCheckerZipGet() {
        return AdminController.honorCheckerZipGet;
    }

    @Override
    public Handler sectionsGet() {
        return AdminController.sectionsGet;
    }

    // AuthController

    @Override
    public Handler verifyAuthenticatedMiddleware() {
        return AuthController.verifyAuthenticatedMiddleware;
    }

    @Override
    public Handler verifyAdminMiddleware() {
        return AuthController.verifyAdminMiddleware;
    }

    @Override
    public Handler meGet() {
        return AuthController.meGet;
    }

    // CasController

    @Override
    public Handler callbackGet() {
        return CasController.callbackGet;
    }

    @Override
    public Handler loginGet() {
        return CasController.loginGet;
    }

    @Override
    public Handler logoutPost() {
        return CasController.logoutPost;
    }

    // ConfigController

    @Override
    public Handler getConfigAdmin() {
        return ConfigController.getConfigAdmin;
    }

    @Override
    public Handler getConfigStudent() {
        return ConfigController.getConfigStudent;
    }

    @Override
    public Handler updateLivePhases() {
        return ConfigController.updateLivePhases;
    }

    @Override
    public Handler scheduleShutdown() {
        return ConfigController.scheduleShutdown;
    }

    @Override
    public Handler updateBannerMessage() {
        return ConfigController.updateBannerMessage;
    }

    @Override
    public Handler updateCourseIdsPost() {
        return ConfigController.updateCourseIdsPost;
    }

    @Override
    public Handler updateCourseIdsUsingCanvasGet() {
        return ConfigController.updateCourseIdsUsingCanvasGet;
    }

    @Override
    public Handler updatePenalties() {
        return ConfigController.updatePenalties;
    }

    // SubmissionController

    @Override
    public Handler submitPost() {
        return SubmissionController.submitPost;
    }

    @Override
    public Handler adminRepoSubmitPost() {
        return SubmissionController.adminRepoSubmitPost;
    }

    @Override
    public Handler submitGet() {
        return SubmissionController.submitGet;
    }

    @Override
    public Handler latestSubmissionForMeGet() {
        return SubmissionController.latestSubmissionForMeGet;
    }

    @Override
    public Handler submissionXGet() {
        return SubmissionController.submissionXGet;
    }

    @Override
    public Handler latestSubmissionsGet() {
        return SubmissionController.latestSubmissionsGet;
    }

    @Override
    public Handler submissionsActiveGet() {
        return SubmissionController.submissionsActiveGet;
    }

    @Override
    public Handler studentSubmissionsGet() {
        return SubmissionController.studentSubmissionsGet;
    }

    @Override
    public Handler approveSubmissionPost() {
        return SubmissionController.approveSubmissionPost;
    }

    @Override
    public Handler submissionsReRunPost() {
        return SubmissionController.submissionsReRunPost;
    }

    // UserController

    @Override
    public Handler repoPatch() {
        return UserController.repoPatch;
    }

    @Override
    public Handler repoPatchAdmin() {
        return UserController.repoPatchAdmin;
    }

    @Override
    public Handler repoHistoryAdminGet() {
        return UserController.repoHistoryAdminGet;
    }
}
