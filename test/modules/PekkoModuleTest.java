package modules;

import org.apache.pekko.actor.ActorSystem;
import org.junit.Test;

import static org.junit.Assert.*;

public class PekkoModuleTest {

    @Test
    public void testProvideTypedActorSystem() {
        // Create a classic actor system
        ActorSystem classicSystem = ActorSystem.create("test-system");

        // Create module
        PekkoModule module = new PekkoModule();

        // Call method
        org.apache.pekko.actor.typed.ActorSystem<Void> typedSystem =
                module.provideTypedActorSystem(classicSystem);

        // Assertions
        assertNotNull(typedSystem);
        assertEquals("test-system", typedSystem.name());

        // Cleanup
        classicSystem.terminate();
    }
}