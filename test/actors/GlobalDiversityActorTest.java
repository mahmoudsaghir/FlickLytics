package actors;

import com.fasterxml.jackson.databind.JsonNode;
import models.GlobalDiversityResult;
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;
import services.GlobalDiversityService;

import static org.mockito.Mockito.*;

public class GlobalDiversityActorTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testComputeDiversity() {
        GlobalDiversityService mockService = mock(GlobalDiversityService.class);

        JsonNode mockJson = mock(JsonNode.class);
        GlobalDiversityResult expectedResult = mock(GlobalDiversityResult.class);

        when(mockService.compute("movie", mockJson, 10)).thenReturn(expectedResult);

        ActorRef<GlobalDiversityActor.Command> actor = testKit.spawn(GlobalDiversityActor.create(mockService));

        TestProbe<GlobalDiversityResult> probe = testKit.createTestProbe();

        actor.tell(new GlobalDiversityActor.ComputeDiversity("movie", mockJson, 10, probe.getRef()));

        probe.expectMessage(expectedResult);

        verify(mockService).compute("movie", mockJson, 10);
    }
}