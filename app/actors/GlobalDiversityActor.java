package actors;

import models.GlobalDiversityResult;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.pekko.actor.typed.javadsl.Receive;
import services.GlobalDiversityService;

/**
 * Actor responsible for computing Global Diversity metrics.
 *
 * @author Mahmoud Saghir
 */
public class GlobalDiversityActor extends AbstractBehavior<GlobalDiversityActor.Command> {

    private final GlobalDiversityService service;

    /**
     * Marker interface for actor commands.
     *
     * @author Mahmoud Saghir
     */
    public interface Command {}

    /**
     * Command to compute Global Diversity metrics.
     *
     * @author Mahmoud Saghir
     */
    public static class ComputeDiversity implements Command {
        public final String category;
        public final JsonNode detailsAndTranslationRoot;
        public final int targetLanguageConstant;
        public final ActorRef<GlobalDiversityResult> replyTo;

        /**
         * Constructor for ComputeDiversity.
         *
         * @param category movie or tv
         * @param detailsAndTranslationRoot details and translations
         * @param targetLanguageConstant normalization constant
         * @param replyTo replyTor for sending the result back to the sender
         * @author Mahmoud Saghir
         */
        public ComputeDiversity(String category, JsonNode detailsAndTranslationRoot, int targetLanguageConstant,
                                ActorRef<GlobalDiversityResult> replyTo) {
            this.category = category;
            this.detailsAndTranslationRoot = detailsAndTranslationRoot;
            this.targetLanguageConstant = targetLanguageConstant;
            this.replyTo = replyTo;
        }
    }

    /**
     * Creates a new GlobalDiversityActor behavior.
     *
     * @return Behavior for the GlobalDiversityActor
     * @author Mahmoud Saghir
     */
    public static Behavior<Command> create(GlobalDiversityService service) {
        return Behaviors.setup(ctx -> new GlobalDiversityActor(ctx, service));
    }

    /**
     * Constructor for GlobalDiversityActor.
     *
     * @param context Actor context
     * @author Mahmoud Saghir
     */
    private GlobalDiversityActor(ActorContext<Command> context, GlobalDiversityService service) {
        super(context);
        this.service = service;
    }

    /**
     * Creates the receiver behavior for the GlobalDiversityActor.
     *
     * @return Receive behavior for the GlobalDiversityActor
     * @author Mahmoud Saghir
     */
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputeDiversity.class, this::onComputeDiversity)
                .build();
    }

    /**
     * Handles ComputeDiversity command.
     *
     * @param msg ComputeDiversity message
     * @return Behavior after handling the message
     * @author Mahmoud Saghir
     */
    private Behavior<Command> onComputeDiversity(ComputeDiversity msg) {
        GlobalDiversityResult result = service.compute(msg.category, msg.detailsAndTranslationRoot,
                msg.targetLanguageConstant);

        // send a result back to the sender
        msg.replyTo.tell(result);

        return this;
    }
}