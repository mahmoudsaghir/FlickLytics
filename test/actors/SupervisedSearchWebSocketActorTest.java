package actors;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.Props;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import services.TmdbService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests for supervised search WebSocket proxy actor.
 *
 * @author Tasmia Naomi
 */
public class SupervisedSearchWebSocketActorTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("SupervisedSearchWebSocketActorTestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    private Map<Integer, String> movieGenres() {
        return new HashMap<>();
    }

    private Map<Integer, String> tvGenres() {
        return new HashMap<>();
    }

    public static class DeferredReplySupervisorActor extends AbstractActor {
        public enum Release { INSTANCE }

        private ActorRef pendingReplyTo;
        private final ActorRef childRef;

        public DeferredReplySupervisorActor(ActorRef childRef) {
            this.childRef = childRef;
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchAny(message -> {
                        if (message instanceof SupervisorActor.CreateSearchActor) {
                            pendingReplyTo = getSender();
                        } else if (message == Release.INSTANCE && pendingReplyTo != null) {
                            pendingReplyTo.tell(childRef, getSelf());
                            pendingReplyTo = null;
                        }
                    })
                    .build();
        }
    }

    @Test
    public void testBuffersMessagesUntilChildReadyThenFlushesInOrder() {
        new TestKit(system) {{
            TmdbService tmdbService = mock(TmdbService.class);

            ActorRef supervisorStub = system.actorOf(
                    Props.create(DeferredReplySupervisorActor.class, () ->
                            new DeferredReplySupervisorActor(getRef()))
            );

            ActorRef proxy = system.actorOf(SupervisedSearchWebSocketActor.props(
                    getRef(),
                    supervisorStub,
                    tmdbService,
                    "http://api.tmdb.org/3/",
                    "token",
                    movieGenres(),
                    tvGenres()
            ));

            proxy.tell("first", ActorRef.noSender());
            proxy.tell("second", ActorRef.noSender());
            expectNoMessage(duration("300 millis"));

            supervisorStub.tell(DeferredReplySupervisorActor.Release.INSTANCE, ActorRef.noSender());
            assertEquals("first", expectMsgClass(duration("2 seconds"), String.class));
            assertEquals("second", expectMsgClass(duration("2 seconds"), String.class));

            proxy.tell("third", ActorRef.noSender());
            assertEquals("third", expectMsgClass(duration("2 seconds"), String.class));
        }};
    }
}

