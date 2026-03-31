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

import static org.mockito.Mockito.*;

public class GlobalDiversityActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testComputeDiversity() {
        new TestKit(system) {{
            // Mock the service and expected result
            GlobalDiversityService mockService = mock(GlobalDiversityService.class);
            JsonNode mockJson = mock(JsonNode.class);
            GlobalDiversityResult expectedResult = mock(GlobalDiversityResult.class);

            when(mockService.compute("movie", mockJson, 10)).thenReturn(expectedResult);

            // Create the actor
            ActorRef actor = system.actorOf(Props.create(GlobalDiversityActor.class, mockService));

            // Create a probe to receive the reply
            TestKit probe = new TestKit(system);

            // Send the message with probe.getRef() as sender
            actor.tell(new GlobalDiversityActor.ComputeDiversity("movie", mockJson, 10), probe.getRef());

            // Expect the result from the probe
            GlobalDiversityResult result = probe.expectMsgClass(GlobalDiversityResult.class);

            // Verify the computation
            assert(result == expectedResult);
            verify(mockService).compute("movie", mockJson, 10);
        }};
    }
}