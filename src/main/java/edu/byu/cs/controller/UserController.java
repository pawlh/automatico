package edu.byu.cs.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.byu.cs.controller.exception.BadRequestException;
import edu.byu.cs.controller.exception.InternalServerException;
import edu.byu.cs.controller.exception.UnauthorizedException;
import edu.byu.cs.controller.exception.WordOfWisdomViolationException;
import edu.byu.cs.model.RepoUpdate;
import edu.byu.cs.model.User;
import edu.byu.cs.service.UserService;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.util.Collection;

public class UserController {
    public static final Handler repoPatch = ctx -> {
        User user = ctx.sessionAttribute("user");
        if (user == null) {
            throw new UnauthorizedException("No user credentials found");
        }
        applyRepoPatch(user.netId(), null, ctx);
        ctx.result("Successfully updated repoUrl");
    };

    public static final Handler repoPatchAdmin = ctx -> {
        User admin = ctx.sessionAttribute("user");
        if (admin == null) {
            throw new UnauthorizedException("No user credentials found");
        }
        String studentNetId = ctx.pathParam("netId");
        applyRepoPatch(studentNetId, admin.netId(), ctx);
        ctx.result("Successfully updated repoUrl for user: " + studentNetId);
    };

    public static final Handler repoHistoryAdminGet = ctx -> {
        String repoUrl = ctx.queryParam("repoUrl");
        String netId = ctx.queryParam("netId");

        Collection<RepoUpdate> updates = UserService.adminGetRepoHistory(repoUrl, netId);
        ctx.json(updates);
    };

    private static void applyRepoPatch(String studentNetId, String adminNetId, Context ctx)
            throws WordOfWisdomViolationException, InternalServerException, BadRequestException {
        JsonObject jsonObject = new Gson().fromJson(ctx.body(), JsonObject.class);
        String repoUrl = new Gson().fromJson(jsonObject.get("repoUrl"), String.class);
        UserService.updateRepoUrl(studentNetId, repoUrl, adminNetId);
    }
}
