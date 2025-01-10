package edu.byu.cs.server.endpointprovider;

import spark.Filter;
import spark.Route;

public interface EndpointProvider {

    // Wildcard endpoints

    Filter beforeAll();
    Filter afterAll();

    Route defaultGet();

    // AdminController

    Route usersGet();
    Route testModeGet();
    Route commitAnalyticsGet();
    Route honorCheckerZipGet();
    Route sectionsGet();

    // AuthController

    Filter verifyAuthenticatedMiddleware();
    Filter verifyAdminMiddleware();
    Route meGet();

    // CasController

    Route callbackGet();
    Route loginGet();
    Route logoutPost();

    // ConfigController

    Route getConfigAdmin();
    Route getConfigStudent();
    Route updateLivePhases();
    Route scheduleShutdown();
    Route updateBannerMessage();
    Route updateCourseIdsPost();
    Route updateCourseIdsUsingCanvasGet();
    Route updatePenalties();

    // SubmissionController

    Route submitPost();
    Route adminRepoSubmitPost();
    Route submitGet();
    Route latestSubmissionForMeGet();
    Route submissionXGet();
    Route latestSubmissionsGet();
    Route submissionsActiveGet();
    Route studentSubmissionsGet();
    Route approveSubmissionPost();
    Route submissionsReRunPost();

    // UserController

    Route setRepoUrl();
    Route setRepoUrlAdmin();
    Route repoHistoryAdminGet();
}
