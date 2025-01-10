package edu.byu.cs.server;

import edu.byu.cs.controller.WebSocketController;
import edu.byu.cs.server.endpointprovider.EndpointProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import static spark.Spark.*;

public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final EndpointProvider provider;

    public Server(EndpointProvider endpointProvider) {
        this.provider = endpointProvider;
    }

    public int start() {
        return start(0);
    }

    public int start(int desiredPort) {
        int chosenPort = setupEndpoints(desiredPort);
        LOGGER.info("Server started on port {}", chosenPort);
        return chosenPort;
    }

    public void stop() {
        Spark.stop();
    }

    private int setupEndpoints(int port) {
        port(port);

        webSocket("/ws", WebSocketController.class);
        webSocketIdleTimeoutMillis(300000);

        staticFiles.location("/frontend/dist");

        before(provider.beforeAll());

        path("/auth", () -> {
            get("/callback", provider.callbackGet());
            get("/login", provider.loginGet());

            // all routes after this point require authentication
            post("/logout", provider.logoutPost());
        });

        path("/api", () -> {
            before("/*", (req, res) -> {
                if (!req.requestMethod().equals("OPTIONS")) provider.verifyAuthenticatedMiddleware().handle(req, res);
            });

            post("/repo", provider.setRepoUrl());

            get("/submit", provider.submitGet());
            post("/submit", provider.submitPost());

            get("/latest", provider.latestSubmissionForMeGet());

            get("/submission", provider.submissionXGet());
            get("/submission/:phase", provider.submissionXGet());

            get("/me", provider.meGet());

            get("/config", provider.getConfigStudent());

            path("/admin", () -> {
                before("/*", (req, res) -> {
                    if (!req.requestMethod().equals("OPTIONS")) provider.verifyAdminMiddleware().handle(req, res);
                });

                post("/repo/:netId", provider.setRepoUrlAdmin());

                get("/repo/history", provider.repoHistoryAdminGet());

                get("/users", provider.usersGet());

                post("/submit", provider.adminRepoSubmitPost());

                path("/submissions", () -> {
                    post("/approve", provider.approveSubmissionPost());

                    get("/latest", provider.latestSubmissionsGet());

                    get("/latest/:count", provider.latestSubmissionsGet());

                    get("/active", provider.submissionsActiveGet());

                    get("/student/:netId", provider.studentSubmissionsGet());

                    post("/rerun", provider.submissionsReRunPost());
                });

                get("/test_mode", provider.testModeGet());

                get("/analytics/commit", provider.commitAnalyticsGet());

                get("/analytics/commit/:option", provider.commitAnalyticsGet());

                get("/honorChecker/zip/:section", provider.honorCheckerZipGet());

                get("/sections", provider.sectionsGet());

                path("/config", () -> {
                    get("", provider.getConfigAdmin());

                    post("/phases", provider.updateLivePhases());
                    post("/phases/shutdown", provider.scheduleShutdown());
                    post("/banner", provider.updateBannerMessage());

                    post("/courseIds", provider.updateCourseIdsPost());
                    get("/courseIds", provider.updateCourseIdsUsingCanvasGet());

                    post("/penalties", provider.updatePenalties());
                });
            });
        });

        // spark's notFound method does not work
        get("/*", provider.defaultGet());

        after(provider.afterAll());

        init();

        awaitInitialization();

        return port();
    }
}
