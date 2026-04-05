package actors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

import java.util.HashSet;
import java.util.Set;

/**
 * Per-WebSocket-connection actor for the movie/TV details live stream.
 *
 * Responsibilities:
 *  1. Filter incoming broadcast items by mediaType ("movie" or "tv")
 *  2. Deduplicate items by their TMDb id so the same item is never pushed twice
 *  3. Forward matching, unseen items to the WebSocket output channel (out)
 *
 * @author Zenghui WU
 */
public class MediaDetailsActor extends AbstractBehavior<MediaDetailsActor.Command> {
    //base type for all message this actor
    public interface Command{}
/*
* sent by the stream subscription for every item that arrives on the shared BroadcastHub
* the actor decides whether to forward it.
* */
    public record NewItem(ObjectNode item) implements  Command{}
    /*
    * send by the inbound webscoket sink when the browser changes the filter
    * user navigates from a movie to TV
    */
    public record ChangeType(String type) implements Command{}
    /*
    * sent when the websocket stream completes (browser disconnects)
    * causes the actor to stop itself
    * */
    public enum Stop implements  Command{INSTANCE}
    //Internal state

    /*Websocket output channel - everything told to this ref reaches the browser*/
    private final ActorRef<JsonNode> out;
    // current filter: movie or tv;
    private String mediaType;

    /*TMDb IDs already forwarded in this session.
    * prevents the same item appearing twice when the broadcast re-emits it.*/
    private final Set<String> seenIds = new HashSet<>();
    //---factory---
    /**
     * Creates the initial Behavior for this actor.
     *
     * @param out       ActorRef connected to the WebSocket outbound source
     * @param mediaType "movie" or "tv"
     * @return Behavior ready to receive Command messages
     * @author Zenghui WU
     */

    public static Behavior<Command> create (ActorRef<JsonNode> out, String mediaType){
        return Behaviors.setup(ctx -> new MediaDetailsActor(ctx, out, mediaType));
    }
    private MediaDetailsActor(ActorContext<Command> ctx, ActorRef<JsonNode> out, String mediaType){
        super(ctx);
        this.out = out;
        this.mediaType = mediaType;

    }
    //---message dispatch---


    @Override
    public Receive<MediaDetailsActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(NewItem.class, this::onNewItem)
                .onMessage(ChangeType.class, this::onChangeType)
                .onMessageEquals(Stop.INSTANCE, Behaviors::stopped)
                .build();
    }
    /**
     * Handles a new item arriving from the broadcast hub.
     * Forwards it only if it matches the current mediaType and has not been sent before.
     *
     * @param msg the incoming NewItem message
     * @return this (same behavior, updated seenIds state)
     * @author Zenghui WU
     */
    private Behavior<Command> onNewItem(NewItem msg){
        ObjectNode item = msg.item();
        String id = item.path("id").asText("");
        String itemType = item.path("type").asText("");

        //only forward if type matches and have not sent this id before
        if(mediaType.equals(itemType) && !id.isEmpty()&&seenIds.add(id)){
            out.tell(item);
        }
        return this;
    }
    /**
     * Handles a type-change request from the browser.
     * Clears the dedup set so the new filter starts fresh.
     *
     * @param msg the ChangeType message containing the new type string
     * @return this (same behavior, cleared seenIds)
     * @author Zenghui WU
     */

    private Behavior<Command> onChangeType(ChangeType msg){
        this.mediaType = msg.type();
        this.seenIds.clear();
        return this;
    }

}
