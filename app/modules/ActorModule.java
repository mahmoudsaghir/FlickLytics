package modules;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import actors.SupervisorActor;
import org.apache.pekko.actor.Props;
import services.GlobalDiversityService;
import services.ReviewsService;
import services.TmdbService;
import com.typesafe.config.Config;

import javax.inject.Inject;
import javax.inject.Provider;

import actors.FinancialPerformanceActor;

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
     * @author Charles Wang (financialActor)
     */
    @Override
    protected void configure() {
        bind(ActorRef.class)
            .annotatedWith(Names.named("supervisorActor"))
            .toProvider(GlobalDiversityActorProvider.class);

        bind(ActorRef.class)
                .annotatedWith(Names.named("financialActor"))
                .toProvider(FinancialPerformanceActorProvider.class);
    }

    /**
     * Provider for the SupervisorActor, which manages child actors for global diversity computations.
     *
     * @author Mahmoud Saghir
     */
    public static class GlobalDiversityActorProvider implements Provider<ActorRef> {

        private final ActorSystem actorSystem;
        private final GlobalDiversityService service;
        private final ReviewsService reviewsService;
        private final TmdbService tmdbService;
        private final String apiUrl;
        private final String tmdbToken;
        private final Config config;

        /**
         * Constructor for GlobalDiversityActorProvider.
         *
         * @param actorSystem ActorSystem instance for creating actors
         * @param service GlobalDiversityService instance for computing diversity metrics
         * @param reviewsService ReviewsService instance for fetching review data
         * @param tmdbService TmdbService instance for TMDb API interactions
         * @param config Config instance for accessing configuration values
         * @author Mahmoud Saghir
         */
        @Inject
        public GlobalDiversityActorProvider(ActorSystem actorSystem,
                                            GlobalDiversityService service,
                                            ReviewsService reviewsService,
                                            TmdbService tmdbService,
                                            Config config) {
            this.actorSystem = actorSystem;
            this.service = service;
            this.reviewsService = reviewsService;
            this.tmdbService = tmdbService;
            this.config = config;
            this.apiUrl = config.getString("tmdb.api.url");
            this.tmdbToken = config.getString("tmdb.api.key");
        }

        /**
         * Creates and returns an ActorRef for the SupervisorActor, which will manage child actors for global diversity computations.
         *
         * @return ActorRef for the SupervisorActor
         * @author Mahmoud Saghir
         */
        @Override
        public ActorRef get() {
            return actorSystem.actorOf(
                    Props.create(
                            SupervisorActor.class,
                            () -> new SupervisorActor(service, reviewsService, tmdbService, apiUrl, tmdbToken)
                    ),
                    "supervisor-actor"
            );
        }
    }

    /**
     * Provider for the FinancialPerformanceActor, which computes financial performance metrics for movies and TV shows.
     *
     * @author Charles Wang
     */
    public static class FinancialPerformanceActorProvider implements Provider<ActorRef> {

        private final ActorSystem actorSystem;

        /**
         * Constructor for FinancialPerformanceActorProvider.
         *
         * @param actorSystem ActorSystem instance for creating actors
         * @author Charles Wang
         */
        @Inject
        public FinancialPerformanceActorProvider(ActorSystem actorSystem) {
            this.actorSystem = actorSystem;
        }

        /**
         * Creates and returns an ActorRef for the FinancialPerformanceActor, which will compute financial performance metrics for movies and TV shows.
         *
         * @return ActorRef for the FinancialPerformanceActor
         * @author Charles Wang
         */
        @Override
        public ActorRef get() {
            return actorSystem.actorOf(
                    FinancialPerformanceActor.props(),
                    "financial-performance-actor"
            );
        }
    }
}