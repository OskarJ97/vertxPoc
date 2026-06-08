package com.example.handler;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class AuthHandler {

    private final MongoClient mongoClient;
    private final JWTAuth jwtAuth;

    public AuthHandler(MongoClient mongoClient, JWTAuth jwtAuth) {
        this.mongoClient = mongoClient;
        this.jwtAuth = jwtAuth;
    }

    public void register(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        if (body == null || !body.containsKey("login") || !body.containsKey("password")) {
            ctx.response().setStatusCode(400).end("Missing login or password");
            return;
        }

        String login = body.getString("login");
        String password = body.getString("password");

        mongoClient.findOne("users", new JsonObject().put("login", login), null)
            .onSuccess(existing -> {
                if (existing != null) {
                    ctx.response().setStatusCode(409).end("Login already taken");
                    return;
                }
                JsonObject user = new JsonObject()
                    .put("_id", UUID.randomUUID().toString())
                    .put("login", login)
                    .put("password", hashPassword(password));

                mongoClient.insert("users", user)
                    .onSuccess(id -> ctx.response().setStatusCode(204).end())
                    .onFailure(err -> ctx.response().setStatusCode(500).end(err.getMessage()));
            })
            .onFailure(err -> ctx.response().setStatusCode(500).end(err.getMessage()));
    }

    public void login(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        if (body == null || !body.containsKey("login") || !body.containsKey("password")) {
            ctx.response().setStatusCode(400).end("Missing login or password");
            return;
        }

        String login = body.getString("login");
        String password = body.getString("password");

        mongoClient.findOne("users", new JsonObject().put("login", login), null)
            .onSuccess(user -> {
                if (user == null || !hashPassword(password).equals(user.getString("password"))) {
                    ctx.response().setStatusCode(401).end("Invalid credentials");
                    return;
                }

                String userId = user.getString("_id");
                String token = jwtAuth.generateToken(
                    new JsonObject().put("sub", userId).put("login", login),
                    new JWTAuthOptions().getJWTOptions().setExpiresInMinutes(60)
                );

                ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("token", token).encode());
            })
            .onFailure(err -> ctx.response().setStatusCode(500).end(err.getMessage()));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
