package actors;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.PoisonPill;
import org.apache.pekko.actor.Props;
import org.apache.pekko.pattern.Patterns;
import services.TmdbService;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Per-WebSocket proxy actor that asks SupervisorActor to create a SearchWebSocketActor child.
 * This keeps search failures under the custom supervisor strategy.
 *
 * @author Tasmia Naomi
 */
public class SupervisedSearchWebSocketActor extends AbstractActor {

    private static final int MAX_BUFFERED_MESSAGES = 32;

    private static final class ChildReady {
        private final ActorRef ref;

        private ChildReady(ActorRef ref) {
            this.ref = ref;
        }
    }

    private static final class ChildCreationFailed {
        private final Throwable cause;

        private ChildCreationFailed(Throwable cause) {
            this.cause = cause;
        }
    }

    private final ActorRef out;
    private final ActorRef supervisorActor;
    private final TmdbService tmdbService;
    private final String apiUrl;
    private final String token;
    private final Map<Integer, String> movieGenres;
    private final Map<Integer, String> tvGenres;

    private final Deque<String> pendingMessages = new ArrayDeque<>();
    private ActorRef delegatedSearchActor;

    public static Props props(ActorRef out,
                              ActorRef supervisorActor,
                              TmdbService tmdbService,
                              String apiUrl,
                              String token,
                              Map<Integer, String> movieGenres,
                              Map<Integer, String> tvGenres) {
        return Props.create(
                SupervisedSearchWebSocketActor.class,
                () -> new SupervisedSearchWebSocketActor(
                        out, supervisorActor, tmdbService, apiUrl, token, movieGenres, tvGenres
                )
        );
    }

    private SupervisedSearchWebSocketActor(ActorRef out,
                                           ActorRef supervisorActor,
                                           TmdbService tmdbService,
                                           String apiUrl,
                                           String token,
                                           Map<Integer, String> movieGenres,
                                           Map<Integer, String> tvGenres) {
        this.out = out;
        this.supervisorActor = supervisorActor;
        this.tmdbService = tmdbService;
        this.apiUrl = apiUrl;
        this.token = token;
        this.movieGenres = movieGenres;
        this.tvGenres = tvGenres;
    }

    @Override
    public void preStart() {
        Props searchProps = SearchWebSocketActor.props(out, tmdbService, apiUrl, token, movieGenres, tvGenres);

        Patterns.ask(
                        supervisorActor,
                        new SupervisorActor.CreateSearchActor(out, searchProps),
                        Duration.ofSeconds(3)
                )
                .thenApply(ActorRef.class::cast)
                .whenComplete((ref, ex) -> {
                    if (ex != null) {
                        getSelf().tell(new ChildCreationFailed(ex), ActorRef.noSender());
                        return;
                    }
                    getSelf().tell(new ChildReady(ref), ActorRef.noSender());
                });
    }

    @Override
    public void postStop() {
        if (delegatedSearchActor != null) {
            delegatedSearchActor.tell(PoisonPill.getInstance(), getSelf());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ChildReady.class, this::onChildReady)
                .match(ChildCreationFailed.class, this::onChildCreationFailed)
                .match(String.class, this::onClientMessage)
                .build();
    }

    private void onChildReady(ChildReady msg) {
        delegatedSearchActor = msg.ref;
        while (!pendingMessages.isEmpty()) {
            delegatedSearchActor.tell(pendingMessages.removeFirst(), getSelf());
        }
    }

    private void onChildCreationFailed(ChildCreationFailed msg) {
        getContext().getSystem().log().error("Failed to create supervised search actor", msg.cause);
        out.tell("{\"type\":\"error\",\"message\":\"Failed to initialize search actor\"}", getSelf());
        getContext().stop(getSelf());
    }

    private void onClientMessage(String message) {
        if (delegatedSearchActor != null) {
            delegatedSearchActor.tell(message, getSelf());
            return;
        }

        if (pendingMessages.size() >= MAX_BUFFERED_MESSAGES) {
            pendingMessages.removeFirst();
        }
        pendingMessages.addLast(message);
    }
}

