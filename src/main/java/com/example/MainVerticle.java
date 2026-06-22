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
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject mongoConfig = new JsonObject()
            .put("connection_string", config().getString("mongo.connection_string", "mongodb://localhost:27017"))
            .put("db_name", config().getString("mongo.db_name", "microservice"));

        MongoClient mongoClient = MongoClient.create(vertx, mongoConfig);

        String jwtSecret = config().getString("jwt.secret");
        if (jwtSecret == null || jwtSecret.isBlank()) {
            startPromise.fail("Missing required configuration: jwt.secret");
            return;
        }

        JWTAuth jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
            .addPubSecKey(new PubSecKeyOptions()
                .setAlgorithm("HS256")
                .setBuffer(jwtSecret)));

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        AuthHandler authHandler = new AuthHandler(mongoClient, jwtAuth);
        ItemHandler itemHandler = new ItemHandler(mongoClient);

        router.post("/register").handler(authHandler::register);
        router.post("/login").handler(authHandler::login);

        router.route("/items*").handler(JWTAuthHandler.create(jwtAuth));
        router.post("/items").handler(itemHandler::create);
        router.get("/items").handler(itemHandler::list);

        ErrorHandler errorHandler = new ErrorHandler();
        router.route().failureHandler(errorHandler::handle);

        mongoClient.createIndexWithOptions(
                "users",
                new JsonObject().put("login", 1),
                new IndexOptions().unique(true))
            .onSuccess(v -> {
                log.info("Index on users.login created (or already exists)");
                vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(3000)
                    .<Void>mapEmpty()
                    .onComplete(startPromise);
            })
            .onFailure(startPromise::fail);
    }
}
