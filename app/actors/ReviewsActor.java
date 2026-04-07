package actors;

import models.ReviewsSummary;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ReviewsService;

/**
 * Actor responsible for computing review sentiment summaries.
 *
 * @author Tasmia Naomi
 */
public class ReviewsActor extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(ReviewsActor.class);

    /**
     * Command message for review summary computation.
     */
    public static class ComputeReviews {
        public final String type;
        public final Long id;

        public ComputeReviews(String type, Long id) {
            this.type = type;
            this.id = id;
        }
    }

    private final ReviewsService reviewsService;
    private final String apiUrl;
    private final String tmdbToken;

    public static Props props(ReviewsService reviewsService, String apiUrl, String tmdbToken) {
        return Props.create(ReviewsActor.class, () -> new ReviewsActor(reviewsService, apiUrl, tmdbToken));
    }

    public ReviewsActor(ReviewsService reviewsService, String apiUrl, String tmdbToken) {
        this.reviewsService = reviewsService;
        this.apiUrl = apiUrl;
        this.tmdbToken = tmdbToken;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ComputeReviews.class, this::onComputeReviews)
                .build();
    }

    private void onComputeReviews(ComputeReviews msg) {
        try {
            ReviewsSummary summary = reviewsService.getReviewsWithSentiment(apiUrl, tmdbToken, msg.type, msg.id);
            getSender().tell(summary, getSelf());
        } catch (Exception e) {
            logger.error("Failed to compute reviews for type={} id={}", msg.type, msg.id, e);
            throw new RuntimeException(e);
        }
    }
}

