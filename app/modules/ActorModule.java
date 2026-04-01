package modules;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import actors.GlobalDiversityActor;
import services.GlobalDiversityService;

import javax.inject.Inject;
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

        @Inject
        public GlobalDiversityActorProvider(ActorSystem actorSystem,
                                            GlobalDiversityService service) {
            this.actorSystem = actorSystem;
            this.service = service;
        }

        @Override
        public ActorRef get() {
            return actorSystem.actorOf(
                    GlobalDiversityActor.props(service),
                    "global-diversity-actor"
            );
        }
    }
}