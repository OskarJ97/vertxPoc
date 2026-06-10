package com.example.handler;

import com.example.exception.AppException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    public void handle(RoutingContext ctx) {
        Throwable failure = ctx.failure();
        int statusCode = ctx.statusCode();

        // Błąd aplikacyjny
        if (failure instanceof AppException ex) {
            ctx.response()
                .setStatusCode(ex.getStatusCode())
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encode());
            return;
        }

        // Błąd HTTP
        if (statusCode == 401 || statusCode == 403 || statusCode == 404) {
            ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject().put("error", "Unauthorized").encode());
            return;
        }

        // Błąd nieoczekiwany
        log.error("Unhandled failure on {} {}: {}",
            ctx.request().method(), ctx.request().path(),
            failure != null ? failure.getMessage() : "unknown", failure);
        ctx.response()
            .setStatusCode(500)
            .putHeader("Content-Type", "application/json")
            .end(new JsonObject().put("error", "Internal server error").encode());
    }
}
