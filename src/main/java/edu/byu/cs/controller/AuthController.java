package edu.byu.cs.controller;

import spark.Filter;

import static edu.byu.cs.controller.security.JwtUtils.validateToken;
import static spark.Spark.halt;

public class AuthController {
    /**
     * A Spark filter that verifies that the request has a valid JWT in the Authorization header.
     * If the request is valid, the netId is added to the session for later use.
     */
    public static Filter verifyAuthenticatedMiddleware = (req, res) -> {
        String token = req.headers("Authorization");
        if (token == null) {
            halt(401);
            return;
        }

        if (!token.startsWith("Bearer ")) {
            halt(401);
            return;
        }

        String jwt = token.substring(7);
        String netId = validateToken(jwt);

        if (netId == null) {
            halt(401);
            return;
        }

        req.session().attribute("netId", netId);
    };

}
