package edu.byu.cs.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.byu.cs.controller.httpexception.BadRequestException;
import edu.byu.cs.controller.httpexception.InternalServerException;
import edu.byu.cs.dataAccess.DataAccessException;
import edu.byu.cs.model.*;
import edu.byu.cs.service.ConfigService;
import io.javalin.http.Handler;

import java.util.ArrayList;

import static edu.byu.cs.controller.HandlerWrapper.*;

public class ConfigController {

    public static final Handler getConfigAdmin = ctx -> {
        try {
            JsonObject response = ConfigService.getPrivateConfig();

            // TODO unserialize or something...?
            // Original was simply `return response;`
            ctx.json(response);

        } catch (DataAccessException e) {
            throw new InternalServerException(e.getMessage(), e);
        }
    };

    public static final Handler getConfigStudent = ctx -> ctx.result(ConfigService.getPublicConfig().toString());

    public static final Handler updateLivePhases = withUser((ctx, user) -> {
        JsonObject jsonObject = ctx.bodyAsClass(JsonObject.class);
        ArrayList phasesArray = new Gson().fromJson(jsonObject.get("phases"), ArrayList.class);

        ConfigService.updateLivePhases(phasesArray, user);
    });

    public static final Handler updateBannerMessage = withUser((ctx, user) -> {
        JsonObject jsonObject = ctx.bodyAsClass(JsonObject.class);

        Gson gson = new Gson();
        String expirationString = gson.fromJson(jsonObject.get("bannerExpiration"), String.class);
        String message = gson.fromJson(jsonObject.get("bannerMessage"), String.class);
        String link = gson.fromJson(jsonObject.get("bannerLink"), String.class);
        String color = gson.fromJson(jsonObject.get("bannerColor"), String.class);

        try {
            ConfigService.updateBannerMessage(user, expirationString, message, link, color);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    });

    public static final Handler updateCourseIdsPost = withUser((ctx, user) -> {
        SetCourseIdsRequest setCourseIdsRequest = ctx.bodyAsClass(SetCourseIdsRequest.class);
        try {
            ConfigService.updateCourseIds(user, setCourseIdsRequest);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    });

    public static final Handler updateCourseIdsUsingCanvasGet = withUser(
            (ctx, user) -> ConfigService.updateCourseIdsUsingCanvas(user)
    );
}
