package com.example;

import com.example.handler.AuthHandler;
import com.example.handler.ErrorHandler;
import com.example.handler.ItemHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject mongoConfig = new JsonObject()
            .put("connection_string", config().getString("mongo.connection_string", "mongodb://localhost:27017"))
            .put("db_name", config().getString("mongo.db_name", "microservice"));

        MongoClient mongoClient = MongoClient.create(vertx, mongoConfig);

        JWTAuth jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
            .addPubSecKey(new PubSecKeyOptions()
                .setAlgorithm("HS256")
                .setBuffer(config().getString("jwt.secret", "super-secret-key-must-be-at-least-32-chars!"))));

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        AuthHandler authHandler = new AuthHandler(mongoClient, jwtAuth);
        ItemHandler itemHandler = new ItemHandler(mongoClient);

        router.post("/register").handler(authHandler::register);
        router.post("/login").handler(authHandler::login);

        // All /items routes require a valid JWT
        router.route("/items*").handler(JWTAuthHandler.create(jwtAuth));
        router.post("/items").handler(itemHandler::create);
        router.get("/items").handler(itemHandler::list);

        ErrorHandler errorHandler = new ErrorHandler();
        router.route().failureHandler(errorHandler::handle);

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(3000)
            .<Void>mapEmpty()
            .onComplete(startPromise);
    }
}
