package edu.byu.cs.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.byu.cs.controller.exception.BadRequestException;
import edu.byu.cs.controller.exception.InternalServerException;
import edu.byu.cs.controller.exception.PriorRepoClaimBlockageException;
import edu.byu.cs.model.RepoUpdate;
import edu.byu.cs.model.User;
import edu.byu.cs.service.UserService;
import edu.byu.cs.util.Serializer;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Collection;

import static spark.Spark.halt;

public class UserController {
    public static final Route setRepoUrl = (req, res) -> {
        User user = req.session().attribute("user");
        setRepoUrl(user.netId(), null, req, res);
        return "Successfully updated repoUrl";
    };

    public static final Route setRepoUrlAdmin = (req, res) -> {
        User admin = req.session().attribute("user");
        String studentNetId = req.params(":netId");
        setRepoUrl(studentNetId, admin.netId(), req, res);
        return "Successfully updated repoUrl for user: " + studentNetId;
    };

    public static final Route repoHistoryAdminGet = (req, res) -> {
        String repoUrl = req.queryParams("repoUrl");
        String netId = req.queryParams("netId");

        Collection<RepoUpdate> updates;
        try {
            updates = UserService.adminGetRepoHistory(repoUrl, netId);
        } catch (BadRequestException e) {
            halt(422, "You must provide either a repoUrl or a netId");
            return null;
        } catch (InternalServerException e) {
            halt(500, "There was an internal server error getting repo updates");
            return null;
        }

        res.status(200);
        res.type("application/json");

        return Serializer.serialize(updates);
    };

    private static void setRepoUrl(String studentNetId, String adminNetId, Request req, Response res) {
        JsonObject jsonObject = new Gson().fromJson(req.body(), JsonObject.class);
        String repoUrl = new Gson().fromJson(jsonObject.get("repoUrl"), String.class);

        try {
            UserService.updateRepoUrl(studentNetId, repoUrl, adminNetId);
        } catch (BadRequestException e) {
            halt(400, e.getMessage());
        } catch (PriorRepoClaimBlockageException e) {
            halt(418, e.getMessage());
        } catch (InternalServerException e) {
            halt(500, e.getMessage());
        }

        res.status(200);
    }
}
