package services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Singleton;
import org.apache.pekko.stream.javadsl.Source;
import org.apache.pekko.stream.javadsl.SourceQueueWithComplete;

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
}
