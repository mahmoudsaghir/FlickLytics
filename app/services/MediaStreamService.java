package services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.pekko.stream.Materializer;
import org.apache.pekko.stream.OverflowStrategy;
import org.apache.pekko.stream.javadsl.BroadcastHub;
import org.apache.pekko.stream.javadsl.Keep;
import org.apache.pekko.stream.javadsl.Source;
import org.apache.pekko.stream.javadsl.SourceQueueWithComplete;
import play.libs.Json;

/*
 * singleton serivce that owns the shared Pekko Broadcasthub for live movie/tv detail updates
 * any component can call push(item) to emit a result to all currently
 * connected wevsocket clients. each client's mediadetailsactor then filters the broadcast by its own mediatype and
 * dedup set.
 *
 * the broadcastHub is created once at injection time and lives for the lifetime of the application.
 * @author Zenghui WU
 *  */
@Singleton
public class MediaStreamService {

    /*
    * Internal queue - the single point for pushing items into the
    * broadcast hub. Buffer = 512 items; oldest will be dropped on overflow
    * */
    private final SourceQueueWithComplete<ObjectNode> queue;
    /*
    * share source procuded by the broadcasthub
    * every call to liveSource() returns this same materialized source;
    * each subscriber gets its own copy of every element
    * */
    private final Source<ObjectNode, ?> broadcastSource;
    /*
    * constrcuts the service and immediately materializes the
    * queue-> broadcasthub pipeline
    * @param mat Pekko stream Materizlizer (injected by Play/Guice)
    * */
    @Inject
    public MediaStreamService(Materializer mat){
        var pair = Source
                .<ObjectNode> queue(512, OverflowStrategy.dropHead())
                .toMat(BroadcastHub.of(ObjectNode.class,512), Keep.both())
                .run(mat);
        this.queue = pair.first();
        this.broadcastSource = pair.second();
    }
    /**
     * returns the shared broadcast source/
     * each subscriber independently receives every item pushed after it subscribves
     *  @author Zenghui WU
     *  @return a source backed by the broadcasthub
     */
    public Source<ObjectNode,?> liveSource(){
        return broadcastSource;
    }
    /**
     * Pushes a media-detail ObjectNode into the broadcast hub so that all
     * connected WebSocket clients receive it.
     *
     * The node must contain at minimum:
     *   "id"   (String) — TMDb id, used for deduplication in the actor
     *   "type" (String) — "movie" or "tv", used for filtering in the actor
     *
     * Called from HomeController.fetchAndRender() after a successful TMDb fetch.
     *
     * @param item the ObjectNode to broadcast
     * @author Zenghui WU
     */
    public void push(ObjectNode item){
        queue.offer(item);
    }
    /**
     * Convenience factory: builds a broadcast-ready ObjectNode from the raw
     * TMDb details JsonNode that fetchAndRender() already has in hand.
     *
     * Only the fields actually rendered in details.scala.html are included,
     * keeping the WebSocket payload small.
     *
     * @param type    "movie" or "tv"
     * @param details the raw JsonNode returned by TmdbService.getDetails()
     * @param overview the plain-text overview string
     * @return an ObjectNode suitable for push() and for JSON serialisation
     * @author Zenghui WU
     */
    public static ObjectNode buildNode(String type, com.fasterxml.jackson.databind.JsonNode details,String overview ){
        ObjectNode node= Json.newObject();
        //fields used for filtering/ dedup in mediadetailsactor

    }


}
