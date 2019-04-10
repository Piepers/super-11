package me.piepers.super11.application;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.stomp.StompServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.stomp.DestinationFactory;
import io.vertx.reactivex.ext.stomp.StompServer;
import io.vertx.reactivex.ext.stomp.StompServerHandler;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import me.piepers.super11.application.model.StandingsDto;
import me.piepers.super11.domain.Competition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);
    private int port;
    private io.vertx.reactivex.core.Vertx rxVertx;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.rxVertx = new io.vertx.reactivex.core.Vertx(vertx);
        this.port = 8080;
    }

    @Override
    public void start(Future<Void> future) {

        Router router = Router.router(vertx);
        Router subRouter = Router.router(vertx);
        subRouter.route(HttpMethod.GET, "/standings").handler(this::competitionHandler);
        router.mountSubRouter("/api", subRouter);

        StompServerOptions stompServerOptions = new StompServerOptions()
                .setPort(-1)
                .setWebsocketBridge(true)
                .setWebsocketPath("/stomp");

        StompServer stompServer = StompServer
                .create(vertx, stompServerOptions)
                .handler(StompServerHandler.create(vertx));

//                .rxListen()
//                .doOnSuccess(stompServer -> LOGGER.debug("Stomp server has been started successfully"))
//                .doOnError(throwable -> LOGGER.debug("Something went wrong while deploying the stomp server", throwable))
//                .doOnError(throwable -> throwable.printStackTrace())
//                .flatMap(stompServer ->
//                        this.vertx
//                                .createHttpServer(new HttpServerOptions()
//                                        .setWebsocketSubProtocols("v10.stomp, v11.stomp, v12.stomp"))
//                                .websocketHandler(stompServer.webSocketHandler())
//                                .requestHandler(router)
//                                .rxListen(this.port))
//                .doOnSuccess(result -> LOGGER.debug("Http Server has been started on port {}", this.port))
//                .doOnSuccess(result -> rxVertx.setPeriodic(3000, this::handleTimer))
//                .doOnError(throwable -> LOGGER.error("Something went wrong while starting the HTTP server"))
//                .doOnError(throwable -> throwable.printStackTrace())
//                .subscribe(result ->
//                                future.complete(),
//                        throwable -> future.fail(throwable));

        this.vertx
                .createHttpServer(new HttpServerOptions().setWebsocketSubProtocols("v10.stomp, v11.stomp, v12.stomp"))
                .websocketHandler(stompServer.webSocketHandler())
                .requestHandler(router)
                .rxListen(this.port)
                .doOnSuccess(result -> LOGGER.debug("Http Server has been started on port {}", this.port))
                .doOnSuccess(result -> rxVertx.setPeriodic(3000, this::handleTimer))
                .subscribe(result ->
                                future.complete(),
                        throwable -> future.fail(throwable));
    }

    private void handleTimer(Long timerId) {
        LOGGER.debug("Publishing something to the stomp address...");
        rxVertx
                .eventBus()
                .publish("update.standings",
                        new JsonObject().put("test", "test contents"), new DeliveryOptions().addHeader("foo", "bar"));
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
