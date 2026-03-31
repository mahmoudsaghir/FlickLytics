package actors;

import org.apache.pekko.actor.*;
import org.apache.pekko.japi.pf.DeciderBuilder;
import services.GlobalDiversityService;

import java.time.Duration;

public class SupervisorActor extends AbstractActor {

    private final GlobalDiversityService globalDiversityService;

    public static class CreateSearchActor {
        public final ActorRef out;
        public final Props props;

        public CreateSearchActor(ActorRef out, Props props) {
            this.out = out;
            this.props = props;
        }
    }

    private ActorRef globalDiversityActor;

    public SupervisorActor(GlobalDiversityService globalDiversityService) {
        this.globalDiversityService = globalDiversityService;
    }

    @Override
    public void preStart() {
        globalDiversityActor = getContext().actorOf(
            Props.create(GlobalDiversityActor.class, () -> new GlobalDiversityActor(globalDiversityService)),
            "globalDiversityActor"
        );
    }

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