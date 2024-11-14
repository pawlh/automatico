package edu.byu.cs.controller;

import edu.byu.cs.controller.httpexception.UnauthorizedException;
import edu.byu.cs.model.User;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

/**
 * Provides wrapper functions that add common pieces of functionality to Javalin Handlers.
 */
public class HandlerWrapper {
    @FunctionalInterface
    public interface UserHandler {
        void handle(@NotNull Context var1, @NotNull User var2) throws Exception;
    }

    /**
     * Creates a Handler which extracts the "user" session attribute, then calls the provided lambda
     * with the user as a parameter
     */
    public static Handler withUser(UserHandler userHandler) {
        return ctx -> {
            User user = ctx.sessionAttribute("user");
            if (user == null) {
                throw new UnauthorizedException("No user attribute found.");
            }
            userHandler.handle(ctx, user);
        };
    }
}
