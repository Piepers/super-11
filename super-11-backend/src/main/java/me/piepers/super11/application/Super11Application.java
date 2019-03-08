package me.piepers.super11.application;

import io.reactivex.Completable;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;
import me.piepers.super11.Super11UdenStandingsVerticle;
import me.piepers.super11.domain.CompetitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Super11Application extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Super11Application.class);

    @Override
    public void start(Future<Void> startFuture) {

        // Set the main configuration of our application to be used.
        final ConfigStoreOptions mainConfigStore = new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", "config/app-conf.json"));

        final ConfigStoreOptions nonPublicConfigStore = new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", "config/non-public-conf.json"));

        final ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(mainConfigStore)
                .addStore(nonPublicConfigStore);

        final ConfigRetriever configRetriever = ConfigRetriever.create(this.vertx, options);

        configRetriever
                .rxGetConfig()
                .flatMapCompletable(configuration -> {


                    new ServiceBinder(this.vertx.getDelegate())
                            .setAddress(CompetitionService.EVENT_BUS_ADDRESS)
                            .register(CompetitionService.class, CompetitionService.create(this.vertx.getDelegate(), configuration));

                    return Completable
                            .fromAction(() -> LOGGER.debug("Deploying Super 11 Application backend."))
                            .andThen(this.vertx
                                    .rxDeployVerticle(HttpServerVerticle.class.getName(), new DeploymentOptions().setConfig(configuration)))
                            .ignoreElement()
                            .andThen(this.vertx.rxDeployVerticle(Super11UdenStandingsVerticle.class.getName(), new DeploymentOptions().setConfig(configuration)))
                            .ignoreElement();
                })
                .subscribe(() -> {
                    LOGGER.debug("Application deployed successfully.");
                    startFuture.complete();
                }, throwable -> {
                    LOGGER.debug("Application has not been deployed successfully due to:", throwable);
                    startFuture.fail(throwable);
                });

    }
}
