package modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.javadsl.Adapter;

/**
 * Guice module to provide Pekko-specific bindings.
 *
 * @author Mahmoud Saghir
 */
public class PekkoModule extends AbstractModule {
    /**
     * Provides a typed actor system.
     *
     * @param classicSystem The classic actor system.
     * @return The typed actor system.
     */
    @Provides
    public ActorSystem<Void> provideTypedActorSystem(org.apache.pekko.actor.ActorSystem classicSystem) {
        // This uses the Pekko Adapter to convert the Classic system to Typed
        return Adapter.toTyped(classicSystem);
    }
}