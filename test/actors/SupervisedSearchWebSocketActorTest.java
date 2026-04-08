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
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import org.apache.pekko.actor.Status;

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
        private boolean releaseRequested;

        public DeferredReplySupervisorActor(ActorRef childRef) {
            this.childRef = childRef;
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchAny(message -> {
                        if (message instanceof SupervisorActor.CreateSearchActor) {
                            pendingReplyTo = getSender();
                            if (releaseRequested) {
                                pendingReplyTo.tell(childRef, getSelf());
                                pendingReplyTo = null;
                                releaseRequested = false;
                            }
                        } else if (message == Release.INSTANCE) {
                            if (pendingReplyTo != null) {
                                pendingReplyTo.tell(childRef, getSelf());
                                pendingReplyTo = null;
                            } else {
                                releaseRequested = true;
                            }
                        }
                    })
                    .build();
        }
    }

    public static class FailingSupervisorActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(SupervisorActor.CreateSearchActor.class,
                            message -> getSender().tell(new Status.Failure(new RuntimeException("boom")), getSelf()))
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

    @Test
    public void testStopsAndReportsErrorWhenChildCreationFails() {
        new TestKit(system) {{
            TmdbService tmdbService = mock(TmdbService.class);

            ActorRef supervisorStub = system.actorOf(Props.create(FailingSupervisorActor.class));
            ActorRef proxy = system.actorOf(SupervisedSearchWebSocketActor.props(
                    getRef(),
                    supervisorStub,
                    tmdbService,
                    "http://api.tmdb.org/3/",
                    "token",
                    movieGenres(),
                    tvGenres()
            ));

            watch(proxy);

            assertEquals(
                    "{\"type\":\"error\",\"message\":\"Failed to initialize search actor\"}",
                    expectMsgClass(duration("2 seconds"), String.class)
            );
            expectTerminated(duration("2 seconds"), proxy);
        }};
    }

    @Test
    public void testDropsOldestBufferedMessagesWhenBufferLimitExceeded() {
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

            for (int i = 0; i < 35; i++) {
                proxy.tell("m" + i, ActorRef.noSender());
            }

            supervisorStub.tell(DeferredReplySupervisorActor.Release.INSTANCE, ActorRef.noSender());

            for (int i = 3; i < 35; i++) {
                assertEquals("m" + i, expectMsgClass(duration("2 seconds"), String.class));
            }

            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testStopBeforeChildReadyCoversNullDelegatedPostStopBranch() {
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

            watch(proxy);
            system.stop(proxy);
            expectTerminated(duration("2 seconds"), proxy);

            // Ensure no buffered message was flushed after a pre-ready stop.
            assertFalse(msgAvailable());
        }};
    }
}
