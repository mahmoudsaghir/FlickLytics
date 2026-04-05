package modules;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import actors.SupervisorActor;
import services.GlobalDiversityService;
import services.TmdbService;
import com.typesafe.config.Config;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Guice module for binding actors.
 *
 * @author Mahmoud Saghir
 */
public class ActorModule extends AbstractModule {

    /**
     * Configures the actor bindings.
     *
     * @author Mahmoud Saghir
     */
    @Override
    protected void configure() {
        bind(ActorRef.class)
            .annotatedWith(Names.named("supervisorActor"))
            .toProvider(GlobalDiversityActorProvider.class);
    }

    public static class GlobalDiversityActorProvider implements Provider<ActorRef> {

        private final ActorSystem actorSystem;
        private final GlobalDiversityService service;
        private final TmdbService tmdbService;
        private final String apiUrl;
        private final String tmdbToken;
        private final Config config;

        @Inject
        public GlobalDiversityActorProvider(ActorSystem actorSystem,
                                            GlobalDiversityService service,
                                            TmdbService tmdbService,
                                            Config config) {
            this.actorSystem = actorSystem;
            this.service = service;
            this.tmdbService = tmdbService;
            this.config = config;
            this.apiUrl = config.getString("tmdb.api.url");
            this.tmdbToken = config.getString("tmdb.api.key");
        }

        @Override
        public ActorRef get() {
            return actorSystem.actorOf(
                    org.apache.pekko.actor.Props.create(
                            SupervisorActor.class,
                            () -> new SupervisorActor(service, tmdbService, apiUrl, tmdbToken)
                    ),
                    "supervisor-actor"
            );
        }
    }
}