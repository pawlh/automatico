package edu.byu.cs.controller;

import edu.byu.cs.controller.httpexception.BadRequestException;
import edu.byu.cs.controller.netmodel.ApprovalRequest;
import edu.byu.cs.controller.netmodel.GradeRequest;
import edu.byu.cs.dataAccess.*;
import edu.byu.cs.model.Phase;
import edu.byu.cs.model.Submission;
import edu.byu.cs.model.User;
import edu.byu.cs.service.SubmissionService;
import edu.byu.cs.util.Serializer;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static edu.byu.cs.controller.HandlerWrapper.*;

public class SubmissionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionController.class);

    public static final Handler submitPost = getSubmitPostHandler(false);
    public static final Handler adminRepoSubmitPost = getSubmitPostHandler(true);

    private static Handler getSubmitPostHandler(boolean isAdmin) {
        return withUser((ctx, user) -> {
            GradeRequest request = validateAndUnpackRequest(ctx, user);
            if (isAdmin) {
                SubmissionService.adminRepoSubmit(user.netId(), request);
            } else {
                SubmissionService.submit(user, request);
            }
        });
    }

    private static GradeRequest validateAndUnpackRequest(Context ctx, User user) throws DataAccessException, BadRequestException {
        String netId = user.netId();

        if (DaoService.getQueueDao().isAlreadyInQueue(netId)) {
            throw new BadRequestException("You are already in the queue");
        }

        GradeRequest request;
        try {
            request = ctx.bodyAsClass(GradeRequest.class);
        } catch (Serializer.SerializationException e) {
            throw new BadRequestException("Request must be valid json", e);
        }

        if (request == null || request.phase() == null) {
            throw new BadRequestException("Request is invalid");
        }

        if (user.repoUrl() == null && user.role() == User.Role.STUDENT) {
            throw new BadRequestException("Student has not provided repo url");
        }

        return request;
    }

    public static final Handler submitGet = withUser((ctx, user) -> {
        boolean inQueue = SubmissionService.isAlreadyInQueue(user.netId());
        ctx.json(Map.of("inQueue", inQueue));
    });

    public static final Handler latestSubmissionForMeGet = withUser((ctx, user) -> {
        Submission submission = SubmissionService.getLastSubmissionForUser(user.netId());
        ctx.json(submission);
    });

    public static final Handler submissionXGet = withUser((ctx, user) -> {
        String phaseString = ctx.pathParam("phase");
        Phase phase;

        try {
            phase = Phase.valueOf(phaseString);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid phase", e);
            throw new BadRequestException("Invalid phase", e);
        }

        Collection<Submission> submissions = SubmissionService.getXSubmissionsForUser(user.netId(), phase);
        ctx.json(submissions);
    });

    public static final Handler latestSubmissionsGet = ctx -> {
        // TODO add capability to not give a count--in which case, set it to -1, which gets all latest submissions
        //  Probably its own endpoint (same, but with no count parameter)
        String countString = ctx.pathParam("count");
        int count = Integer.parseInt(countString);

        Collection<Submission> submissions = SubmissionService.getLatestSubmissions(count);
        ctx.json(submissions);
    };

    public static final Handler submissionsActiveGet = ctx -> {
        List<String> inQueue = SubmissionService.getActiveInQueue();
        List<String> currentlyGrading = SubmissionService.getCurrentlyGrading();
        ctx.json(Map.of("currentlyGrading", currentlyGrading, "inQueue", inQueue));
    };

    public static final Handler studentSubmissionsGet = ctx -> {
        String netId = ctx.pathParam("netId");
        Collection<Submission> submissions = SubmissionService.getSubmissionsForUser(netId);
        ctx.json(submissions);
    };

    public static final Handler approveSubmissionPost = withUser((ctx, admin) -> {
        ApprovalRequest request = ctx.bodyAsClass(ApprovalRequest.class);
        SubmissionService.approveSubmission(admin.netId(), request);
    });

    public static final Handler submissionsReRunPost = ctx -> {
        SubmissionService.reRunSubmissionsInQueue();
        ctx.json(Map.of("message", "re-running submissions in queue"));
    };

}
