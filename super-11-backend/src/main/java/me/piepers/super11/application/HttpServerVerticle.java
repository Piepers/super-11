package me.piepers.super11.application;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.WebClient;
import me.piepers.super11.reactivex.domain.CompetitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);
    private int port;
    private WebClient webClient;
    private io.vertx.reactivex.core.Vertx rxVertx;
    private List<String> cookies = new ArrayList<>();
    private CompetitionService competitionService;

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

        this.competitionService = CompetitionService.createProxy(rxVertx);
    }

    @Override
    public void start(Future<Void> future) {
        Router router = Router.router(vertx);

        Router subRouter = Router.router(vertx);
        subRouter.route(HttpMethod.POST, "/start").handler(this::startHandler);
        router.mountSubRouter("/api", subRouter);


        this.vertx
                .createHttpServer()
                .requestHandler(router)
                .rxListen(this.port)
                .doOnSuccess(result -> LOGGER.debug("Http Server has been started on port {}", this.port))
                .subscribe(result -> future.complete(),
                        throwable -> future.fail(throwable));
    }

    private void startHandler(RoutingContext routingContext) {
        competitionService
                .rxFetchLatestCompetitionStandings()
                .subscribe(competition -> routingContext
                                .response()
                                .putHeader("Content-Type", "application/json")
                                .end(competition.toJson().encode()),
                        throwable -> routingContext.fail(throwable));

    }
}
