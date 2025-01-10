package edu.byu.cs.server;

import edu.byu.cs.server.endpointprovider.MockEndpointProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class ServerTest {
    private static TestServerFacade serverFacade;
    private static Server server;

    private static final MockEndpointProvider mockedMockProvider = spy(new MockEndpointProvider());

    public static Stream<Arguments> getPathParamEndpoints() {
        return Stream.of(
                Arguments.of( "GET", "/api/submission", "submissionXGet", ":phase"),
                Arguments.of( "GET", "/api/admin/analytics/commit", "commitAnalyticsGet", ":option"),
                Arguments.of("POST", "/api/admin/repo", "setRepoUrlAdmin", ":netid"),
                Arguments.of( "GET", "/api/admin/honorChecker/zip", "honorCheckerZipGet", ":section"),
                Arguments.of( "GET", "/api/admin/submissions/latest", "latestSubmissionsGet", ":count"),
                Arguments.of( "GET", "/api/admin/submissions/student", "studentSubmissionsGet", ":netid")
        );
        // api/admin/config/penalties
    }

    public static Stream<Arguments> getEndpoints() {
        return Stream.of(
                Arguments.of( "GET", "/auth/callback", "callbackGet"),
                Arguments.of( "GET", "/auth/login", "loginGet"),
                Arguments.of("POST", "/auth/logout", "logoutPost"),

                Arguments.of( "GET", "/api/config", "getConfigStudent"),
                Arguments.of( "GET", "/api/latest", "latestSubmissionForMeGet"),
                Arguments.of( "GET", "/api/me", "meGet"),
                Arguments.of("POST", "/api/repo", "setRepoUrl"),
                Arguments.of( "GET", "/api/submission", "submissionXGet"),
                Arguments.of( "GET", "/api/submit", "submitGet"),
                Arguments.of("POST", "/api/submit", "submitPost"),

                Arguments.of( "GET", "/api/admin/config", "getConfigAdmin"),
                Arguments.of("POST", "/api/admin/config/banner", "updateBannerMessage"),
                Arguments.of("POST", "/api/admin/config/courseIds", "updateCourseIdsPost"),
                Arguments.of( "GET", "/api/admin/config/courseIds", "updateCourseIdsUsingCanvasGet"),
                Arguments.of("POST", "/api/admin/config/penalties", "updatePenalties"),
                Arguments.of("POST", "/api/admin/config/phases", "updateLivePhases"),
                Arguments.of("POST", "/api/admin/config/phases/shutdown", "scheduleShutdown"),

                Arguments.of( "GET", "/api/admin/submissions/active", "submissionsActiveGet"),
                Arguments.of("POST", "/api/admin/submissions/approve", "approveSubmissionPost"),
                Arguments.of( "GET", "/api/admin/submissions/latest", "latestSubmissionsGet"),
                Arguments.of("POST", "/api/admin/submissions/rerun", "submissionsReRunPost"),
                Arguments.of("POST", "/api/admin/submit", "adminRepoSubmitPost"),

                Arguments.of( "GET", "/api/admin/analytics/commit", "commitAnalyticsGet"),
                Arguments.of( "GET", "/api/admin/repo/history", "repoHistoryAdminGet"),
                Arguments.of( "GET", "/api/admin/sections", "sectionsGet"),
                Arguments.of( "GET", "/api/admin/test_mode", "testModeGet"),
                Arguments.of( "GET", "/api/admin/users", "usersGet")
                );
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeAll
    public static void init() {
        server = new Server(mockedMockProvider);
        int port = server.start();
        System.out.println("Started test HTTP server on " + port);

        serverFacade = new TestServerFacade("localhost", port);
    }

    @AfterEach
    public void tearDown() {
        reset(mockedMockProvider);
    }

    @ParameterizedTest
    @MethodSource("getEndpoints")
    public void verifyEndpointCallsItsHandlersExactlyOnceInOrder(String method, String path, String endpointName) throws IOException {
        // When
        serverFacade.makeRequest(method, path);

        InOrder inOrder = inOrder(mockedMockProvider);

        // Then
        inOrder.verify(mockedMockProvider, times(1)).runHandler("beforeAll");
        this.verifyInOrder_authenticationMiddleware(path, inOrder);
        inOrder.verify(mockedMockProvider, times(1)).runHandler(endpointName);
        inOrder.verify(mockedMockProvider, times(1)).runHandler("afterAll");
    }

    @ParameterizedTest
    @MethodSource("getPathParamEndpoints")
    public void verifyPathParameterHasAValueWhenGivenOne(String method, String path, String endpointName,
            String pathParamName) throws IOException {
        // Given
        String fullPath = path + "/testParamValue";

        // When
        serverFacade.makeRequest(method, fullPath);

        // Then
        verify(mockedMockProvider, times(1)).hasPathParam(endpointName, pathParamName, "testParamValue");
    }

    private void verifyInOrder_authenticationMiddleware(String path, InOrder inOrder) {
        List<String> pathNodes = Arrays.stream(path.split("/")).toList();

        if (!pathNodes.contains("api")) {
            return;
        }

        if (!pathNodes.contains("auth")) {
            // Requires authentication
            inOrder.verify(mockedMockProvider, times(1)).runHandler("verifyAuthenticatedMiddleware");
        }

        if (pathNodes.contains("admin")) {
            // Requires admin
            inOrder.verify(mockedMockProvider, times(1)).runHandler("verifyAdminMiddleware");
        }
    }

    @Test
    void nonexistent_GET_endpoint_calls_beforeAll_then_defaultGet_then_afterAll_exactly_once_in_order() throws IOException {
        serverFacade.makeRequest("GET", "/iDoNotExist");

        // Verify they ran in order
        InOrder inOrder = inOrder(mockedMockProvider);
        inOrder.verify(mockedMockProvider).runHandler("beforeAll");
        inOrder.verify(mockedMockProvider).runHandler("defaultGet");
        inOrder.verify(mockedMockProvider).runHandler("afterAll");

        // Verify they only ran once
        verify(mockedMockProvider, times(1)).runHandler("beforeAll");
        verify(mockedMockProvider, times(1)).runHandler("defaultGet");
        verify(mockedMockProvider, times(1)).runHandler("afterAll");
    }
}
