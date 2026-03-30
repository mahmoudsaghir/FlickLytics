package actors;

import com.fasterxml.jackson.databind.JsonNode;
import models.GlobalDiversityResult;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.GlobalDiversityService;

/**
 * Actor responsible for computing Global Diversity metrics.
 *
 * @author Mahmoud Saghir
 */
public class GlobalDiversityActor extends AbstractActor {

    private final GlobalDiversityService service;
    private final Logger logger = LoggerFactory.getLogger(GlobalDiversityActor.class);

    /**
     * Command to compute Global Diversity metrics.
     *
     * @author Mahmoud Saghir
     */
    public static class ComputeDiversity {
        public final String category;
        public final JsonNode detailsAndTranslationRoot;
        public final int targetLanguageConstant;
        public final ActorRef replyTo;

        /**
         * Constructor for ComputeDiversity.
         *
         * @param category movie or tv
         * @param detailsAndTranslationRoot details and translations
         * @param targetLanguageConstant normalization constant
         * @param replyTo replyTo for sending the result back to the sender
         * @author Mahmoud Saghir
         */
        public ComputeDiversity(String category, JsonNode detailsAndTranslationRoot, int targetLanguageConstant,
                                ActorRef replyTo) {
            this.category = category;
            this.detailsAndTranslationRoot = detailsAndTranslationRoot;
            this.targetLanguageConstant = targetLanguageConstant;
            this.replyTo = replyTo;
        }
    }

    /**
     * Constructor for GlobalDiversityActor.
     *
     * @param service GlobalDiversityService instance
     * @author Mahmoud Saghir
     */
    public GlobalDiversityActor(GlobalDiversityService service) {
        this.service = service;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ComputeDiversity.class, this::onComputeDiversity)
                .matchAny(o -> logger.warn("Received unknown message: {}", o))
                .build();
    }

    private void onComputeDiversity(ComputeDiversity msg) {
        logger.info("Received ComputeDiversity message: category={}, targetLanguageConstant={}", msg.category, msg.targetLanguageConstant);
        GlobalDiversityResult result = service.compute(msg.category, msg.detailsAndTranslationRoot, msg.targetLanguageConstant);
        logger.info("Computed GlobalDiversityResult: {}", result);
        msg.replyTo.tell(result, getSelf());
    }
}