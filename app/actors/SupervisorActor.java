package actors;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.OneForOneStrategy;
import org.apache.pekko.actor.SupervisorStrategy;
import org.apache.pekko.japi.pf.DeciderBuilder;

import java.time.Duration;

public class SupervisorActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
                10,
                Duration.ofMinutes(1),
                DeciderBuilder
                        .match(Exception.class, e -> SupervisorStrategy.restart())
                        .build()
        );
    }
}