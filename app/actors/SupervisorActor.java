package actors;

import org.apache.pekko.actor.*;
import org.apache.pekko.japi.pf.DeciderBuilder;
import services.GlobalDiversityService;

import java.time.Duration;

/**
 * Supervisor actor responsible for managing the lifecycle of child actors.
 *
 * @author Mahmoud Saghir
 */
public class SupervisorActor extends AbstractActor {

    private final GlobalDiversityService globalDiversityService;

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

    private ActorRef globalDiversityActor;

    /**
     * Constructor for SupervisorActor.
     *
     * @param globalDiversityService
     * @author Mahmoud Saghir
     */
    public SupervisorActor(GlobalDiversityService globalDiversityService) {
        this.globalDiversityService = globalDiversityService;
    }

    /**
     * Creates the GlobalDiversityActor before starting the actor.
     *
     * @author Mahmoud Saghir
     */
    @Override
    public void preStart() {
        globalDiversityActor = getContext().actorOf(
                Props.create(GlobalDiversityActor.class, () -> new GlobalDiversityActor(globalDiversityService)),
                "globalDiversityActor"
        );
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