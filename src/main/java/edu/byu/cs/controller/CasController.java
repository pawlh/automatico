package edu.byu.cs.controller;

import edu.byu.cs.canvas.CanvasException;
import edu.byu.cs.model.User;
import edu.byu.cs.properties.ApplicationProperties;
import edu.byu.cs.service.CasService;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static edu.byu.cs.util.JwtUtils.generateToken;

public class CasController {
    public static final Handler callbackGet = ctx -> {
        String ticket = ctx.queryParam("ticket");

        User user;
        try {
            user = CasService.callback(ticket);
        } catch (CanvasException e) {
            String errorUrlParam = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            ctx.redirect(ApplicationProperties.frontendUrl() + "/login?error=" + errorUrlParam, HttpStatus.FOUND);
            return;
        }

        // FIXME: secure cookie with httpOnly
        ctx.cookie("token", generateToken(user.netId()), 14400);
        ctx.redirect(ApplicationProperties.frontendUrl(), HttpStatus.FOUND);
    };

    public static final Handler loginGet = ctx -> {
        // check if already logged in
        if (ctx.cookie("token") != null) {
            ctx.redirect(ApplicationProperties.frontendUrl(), HttpStatus.FOUND);
            return;
        }
        ctx.redirect(CasService.BYU_CAS_URL + "/login" + "?service=" + ApplicationProperties.casCallbackUrl());
    };

    public static final Handler logoutPost = ctx -> {
        if (ctx.cookie("token") == null) {
            ctx.redirect(ApplicationProperties.frontendUrl(), HttpStatus.UNAUTHORIZED);
            return;
        }

        // TODO: call cas logout endpoint with ticket
        ctx.removeCookie("token", "/");
        ctx.redirect(ApplicationProperties.frontendUrl(), HttpStatus.OK);
    };

}
