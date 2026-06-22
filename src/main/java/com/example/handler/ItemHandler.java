package com.example.handler;

import com.example.exception.ValidationException;
import com.example.util.Validator;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class ItemHandler {

    private final MongoClient mongoClient;

    public ItemHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void create(RoutingContext ctx) {
        String userId = ctx.user().principal().getString("sub");
        JsonObject body = ctx.body().asJsonObject();
        if (body == null) {
            ctx.fail(new ValidationException("Request body is required"));
            return;
        }

        try {
            Validator.validateItemName(body.getString("name"));
        } catch (ValidationException e) {
            ctx.fail(e);
            return;
        }

        JsonObject item = new JsonObject()
            .put("_id", UUID.randomUUID().toString())
            .put("owner", userId)
            .put("name", body.getString("name"));

        mongoClient.insert("items", item)
            .onSuccess(id -> ctx.response().setStatusCode(204).end())
            .onFailure(ctx::fail);
    }

    public void list(RoutingContext ctx) {
        String userId = ctx.user().principal().getString("sub");

        mongoClient.find("items", new JsonObject().put("owner", userId))
            .onSuccess(items -> {
                JsonArray result = new JsonArray();
                for (JsonObject item : items) {
                    result.add(new JsonObject()
                        .put("id", item.getString("_id"))
                        .put("name", item.getString("name")));
                }
                ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(result.encode());
            })
            .onFailure(ctx::fail);
    }
}
