package actors;

import models.MovieOrTVShow;
import org.apache.pekko.actor.*;
import org.apache.pekko.pattern.Patterns;
import org.apache.pekko.japi.pf.DeciderBuilder;
import services.GlobalDiversityService;
import services.TmdbService;

import java.time.Duration;
import java.util.List;

/**
 * Supervisor actor responsible for managing the lifecycle of child actors.
 *
 * @author Mahmoud Saghir
 */
public class SupervisorActor extends AbstractActor {

    private final GlobalDiversityService globalDiversityService;
    private final TmdbService tmdbService;
    private final String apiUrl;
    private final String tmdbToken;

    /**
     * Message to create a child actor.
     *
     * @author Mahmoud Saghir
     */
    public static class CreateSearchActor {
        public final ActorRef out;
        public final Props props;

        /**
         * Constructor for CreateSearchActor.
         *
         * @param out   ActorRef to send messages back to the client
         * @param props Props for creating the child actor
         * @author Mahmoud Saghir
         */
        public CreateSearchActor(ActorRef out, Props props) {
            this.out = out;
            this.props = props;
        }
    }

    /**
     * Message to compute PersonStats via PersonStatsActor.
     *
     * @author Syed Shahab Shah
     */
    public static class ComputePersonStats {
        public final List<MovieOrTVShow> items;
        public final String name;
        public final String profilePath;
        public final String knownForDepartment;
        public final int genderCode;
        public final String birthday;
        public final String placeOfBirth;

        public ComputePersonStats(List<MovieOrTVShow> items,
                                  String name,
                                  String profilePath,
                                  String knownForDepartment,
                                  int genderCode,
                                  String birthday,
                                  String placeOfBirth) {
            this.items = items;
            this.name = name;
            this.profilePath = profilePath;
            this.knownForDepartment = knownForDepartment;
            this.genderCode = genderCode;
            this.birthday = birthday;
            this.placeOfBirth = placeOfBirth;
        }
    }

    private ActorRef globalDiversityActor;
    private ActorRef personStatsActor;

    /**
     * Constructor for SupervisorActor.
     *
     * @param globalDiversityService GlobalDiversityService instance
     * @param tmdbService            TmdbService instance
     * @param apiUrl                 API URL string
     * @param tmdbToken              TMDB token string
     * @author Mahmoud Saghir
     */
    public SupervisorActor(GlobalDiversityService globalDiversityService,
                           TmdbService tmdbService,
                           String apiUrl,
                           String tmdbToken) {
        this.globalDiversityService = globalDiversityService;
        this.tmdbService = tmdbService;
        this.apiUrl = apiUrl;
        this.tmdbToken = tmdbToken;
    }

    /**
     * Creates the GlobalDiversityActor before starting the actor.
     *
     * @author Mahmoud Saghir
     */
    @Override
    public void preStart() {
        globalDiversityActor = getContext().actorOf(
                Props.create(GlobalDiversityActor.class, () ->
                        new GlobalDiversityActor(globalDiversityService, tmdbService, apiUrl, tmdbToken)), "globalDiversityActor"
        );
        personStatsActor = getContext().actorOf(PersonStatsActor.props(null), "personStatsActor");
    }

    /**
     * Creates receive builder for SupervisorActor.
     *
     * @return Receive builder for SupervisorActor
     * @author Mahmoud Saghir
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateSearchActor.class, msg -> {
                    ActorRef child = getContext().actorOf(msg.props);
                    getSender().tell(child, getSelf());
                })
                .match(GlobalDiversityActor.ComputeDiversity.class, msg -> globalDiversityActor.forward(msg, getContext()))
                .match(ComputePersonStats.class, msg -> {
                    ActorRef replyTo = getSender();
                    Patterns.pipe(
                            Patterns.ask(
                                    personStatsActor,
                                    new PersonStatsActor.ComputePersonStats(
                                            msg.items,
                                            msg.name,
                                            msg.profilePath,
                                            msg.knownForDepartment,
                                            msg.genderCode,
                                            msg.birthday,
                                            msg.placeOfBirth
                                    ),
                                    Duration.ofSeconds(3)
                            ),
                            getContext().dispatcher()
                    ).to(replyTo);
                })
                .build();
    }

    /**
     * Supervisor strategy for handling actor failures.
     *
     * @return SupervisorStrategy instance
     * @author Mahmoud Saghir
     */
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
                10,
                Duration.ofMinutes(1),
                DeciderBuilder.match(Exception.class, e -> {
                            System.out.println("Restarting actor due to: " + e.getMessage());
                            return SupervisorStrategy.restart();
                        })
                        .build()
        );
    }
}