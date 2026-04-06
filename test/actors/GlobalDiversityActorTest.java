package actors;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import models.GlobalDiversityResult;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import services.GlobalDiversityService;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the GlobalDiversityActor class.
 *
 * @author Mahmoud Saghir
 */
public class GlobalDiversityActorTest {

    static ActorSystem system;

    /**
     * Setup the test environment.
     *
     * @author Mahmoud Saghir
     */
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    /**
     * Tear down the test environment.
     *
     * @author Mahmoud Saghir
     */
    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Tests the computeDiversity method.
     *
     * @author Mahmoud Saghir
     */
    @Test
    public void testComputeDiversity() {
        new TestKit(system) {{
            // Mock both services
            GlobalDiversityService mockService = mock(GlobalDiversityService.class);
            services.TmdbService mockTmdbService = mock(services.TmdbService.class);
            JsonNode mockJson = mock(JsonNode.class);
            GlobalDiversityResult expectedResult = mock(GlobalDiversityResult.class);

            // Mock tmdbService to return mockJson so we can verify service.compute gets it
            try {
                when(mockTmdbService.getDetailsAndTranslations(
                        eq("fake-url"), eq("fake-token"), eq("movie"), eq(10L)))
                        .thenReturn(mockJson);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Mock service.compute to return expectedResult when called with mockJson
            when(mockService.compute("movie", mockJson, 5)).thenReturn(expectedResult);

            // Create the actor using the correct 4-argument props factory
            ActorRef actor = system.actorOf(
                    GlobalDiversityActor.props(mockService, mockTmdbService, "fake-url", "fake-token"));

            // Create a probe to receive the reply
            TestKit probe = new TestKit(system);

            // Send the message
            actor.tell(new GlobalDiversityActor.ComputeDiversity("movie", 10L, 5), probe.getRef());

            // Expect the result
            GlobalDiversityResult result = probe.expectMsgClass(
                    duration("2 seconds"), GlobalDiversityResult.class);

            // Verify
            assertSame(expectedResult, result);
            try {
                verify(mockTmdbService).getDetailsAndTranslations("fake-url", "fake-token", "movie", 10L);
                verify(mockService).compute("movie", mockJson, 5);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }};
    }
}