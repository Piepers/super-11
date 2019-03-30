package me.piepers.super11.application;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.WebClient;
import me.piepers.super11.application.model.StandingsDto;
import me.piepers.super11.domain.Competition;
import me.piepers.super11.domain.Draft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);
    private int port;
    private WebClient webClient;
    private io.vertx.reactivex.core.Vertx rxVertx;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.rxVertx = new io.vertx.reactivex.core.Vertx(vertx);
        this.port = 8080;
        this.webClient = WebClient.create(rxVertx,
                new WebClientOptions().
                        setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .setMaxPoolSize(10)
                        .setLogActivity(false));
    }

    @Override
    public void start(Future<Void> future) {
        Router router = Router.router(vertx);

        Router subRouter = Router.router(vertx);
        subRouter.route(HttpMethod.GET, "/standings").handler(this::competitionHandler);
        router.mountSubRouter("/api", subRouter);


        this.vertx
                .createHttpServer()
                .requestHandler(router)
                .rxListen(this.port)
                .doOnSuccess(result -> LOGGER.debug("Http Server has been started on port {}", this.port))
                .subscribe(result -> future.complete(),
                        throwable -> future.fail(throwable));
    }

    private void competitionHandler(RoutingContext routingContext) {
        vertx
                .eventBus()
                .<JsonObject>rxSend("get.competition", new JsonObject())
                .map(result -> new Competition(result.body()))
                .map(competition -> competition.getData().getDrafts().stream().map(draft -> StandingsDto.from(draft)).collect(Collectors.toList()))
                .map(list -> new JsonObject().put("drafts", new JsonArray(list)))
                .subscribe(result -> routingContext
                                .response()
                                .setStatusCode(200)
                                .putHeader("Content-Type", "application/json; charset=UTF-8")
                                // FIXME: make more restrict in production.
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(result.encode()),
                        throwable -> routingContext
                                .response()
                                .setStatusCode(500)
                                .putHeader("Content-Type", "application/json; charset=UTF-8")
                                .end(new JsonObject().put("Error", throwable
                                        .getMessage())
                                        .encode(), StandardCharsets.UTF_8.name()));
    }
}
