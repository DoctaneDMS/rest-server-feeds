/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.feed.rest.server;

import com.softwareplumbers.feed.Feed;
import com.softwareplumbers.feed.FeedExceptions;
import com.softwareplumbers.feed.FeedExceptions.InvalidId;
import com.softwareplumbers.feed.FeedExceptions.InvalidPath;
import com.softwareplumbers.feed.FeedExceptions.StreamingException;
import com.softwareplumbers.feed.FeedPath;
import com.softwareplumbers.feed.FeedService;
import com.softwareplumbers.feed.Message;
import com.softwareplumbers.feed.MessageIterator;
import com.softwareplumbers.feed.impl.MessageFactory;
import com.softwareplumbers.rest.server.core.Authenticated;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author jonathan
 */
@Component
@Authenticated
@Path("/feed")
public class Feeds {
    
    private static final XLogger LOG = XLoggerFactory.getXLogger(Feeds.class);
    
    private FeedServiceFactory feedServiceFactory;
    private MessageFactory messageFactory = new MessageFactory();
    
    private static class OutputConsumer implements StreamingOutput {
        private final Consumer<OutputStream> consumer;
        public OutputConsumer(Consumer<OutputStream> consumer) { this.consumer = consumer; }
        @Override
        public void write(OutputStream os) { consumer.accept(os); }
        public static OutputConsumer of(Consumer<OutputStream> consumer) {
            return new OutputConsumer(consumer);
        }
    }
    
    /**
     * Use by Spring to inject a service factory for retrieval of a named repository service.
     * 
     * @param serviceFactory A factory for retrieving named services
     */
    @Autowired
    public void setFeedServiceFactory(FeedServiceFactory serviceFactory) {
        LOG.entry(serviceFactory);
        this.feedServiceFactory = serviceFactory;
    }
        
    private static URI getURI(UriInfo info, String repository, FeedPath path) {
        UriBuilder builder = info.getBaseUriBuilder().path("feed").path(repository);
        path.apply(builder, (bldr,element)->bldr.path(element.toString()));
        return builder.build();
    }
    
    @POST
    @Path("/{repository}/{feed:[^?]+}")
    @Consumes({ MediaType.APPLICATION_OCTET_STREAM })
    public Response post(
            @PathParam("repository") String repository,
            @PathParam("feed") FeedPath feedPath,
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            InputStream data
    ) throws FeedExceptions.BaseException {
        LOG.entry(repository, feedPath);
        FeedService feedService = feedServiceFactory.getService(repository);
        if (feedService == null) throw LOG.throwing(new RuntimeException("feed service not found"));
        Message parsed = messageFactory.build(data, false)
            .orElseThrow(()->LOG.throwing(new RuntimeException("feed service not found")))
            .setSender(securityContext.getUserPrincipal().getName());                
        Message result = feedService.post(feedPath, parsed);
        return LOG.exit(Response
            .created(getURI(uriInfo, repository, result.getName()))
            .entity(OutputConsumer.of(FeedExceptions.runtime(result::writeHeaders)))
            .build());
    }
    
    @PUT
    @Path("/{repository}/{message:[^?]+}")
    @Consumes({ MediaType.APPLICATION_OCTET_STREAM })
    public Response replicate(
            @PathParam("repository") String repository,
            @PathParam("message") FeedPath feedPath,
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            InputStream data
    ) throws FeedExceptions.BaseException {
        LOG.entry(repository, feedPath);
        FeedService feedService = feedServiceFactory.getService(repository);
        if (feedService == null) throw LOG.throwing(new RuntimeException("feed service not found"));
        Message parsed = messageFactory.build(data, false)
            .orElseThrow(()->LOG.throwing(new RuntimeException("feed service not found")))
            .setSender(securityContext.getUserPrincipal().getName());                
        Message result = feedService.replicate(parsed);
        return LOG.exit(Response
            .accepted()
            .entity(OutputConsumer.of(FeedExceptions.runtime(result::writeHeaders)))
            .build());
    }    

    public void resume(AsyncResponse response, MessageIterator messages) {
        LOG.entry(response, messages);
        response.resume(OutputConsumer.of(os -> {
            try {
                int count = 0;
                while (messages.hasNext()) {
                    Message message = messages.next();
                    message.writeHeaders(os);
                    message.writeData(os);
                    count++;
                }
                messages.close();
                LOG.debug("wrote {} messages", count);
            } catch (StreamingException e) {
                throw FeedExceptions.runtime(e);
            } 
        })); 
        LOG.exit();
    }

    public void resume(AsyncResponse response, JsonObject json) {
        LOG.entry(response, json);
        response.resume(OutputConsumer.of(os -> {
            try (JsonWriter writer = Json.createWriter(os)) {
                writer.write(json);
            } 
        })); 
        LOG.exit();
    }
    
    @GET
    @Path("/{repository}/{feed:[^?]+}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
    public void listen(
            @PathParam("repository") String repository,
            @PathParam("feed") FeedPath feedPath,
            @QueryParam("from") Instant fromTime,
            @QueryParam("fromInclusive") @DefaultValue("false") boolean fromInclusive,
            @QueryParam("to") Instant toTime,
            @QueryParam("toInclusive") Boolean toInclusive,
            @QueryParam("wait") @DefaultValue("0") int waitTime,
            @QueryParam("serviceId") UUID serviceId,
            @QueryParam("relay") @DefaultValue("true") Boolean relay,
            @QueryParam("filters") @DefaultValue("") RemoteFilters filters,
            @Context HttpHeaders headers,
            @Suspended AsyncResponse response
    ) throws InvalidPath, InvalidId {
        LOG.entry(repository, feedPath, fromTime, waitTime);
        FeedService feedService = feedServiceFactory.getService(repository);
        
        UUID serviceUid = serviceId == null ? feedService.getServerId() : serviceId;
        if (fromTime == null) fromTime = Instant.now();
        
        if (headers.getAcceptableMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)) {
            // We are asking for Json, so the user wants metadata
            Feed feed = feedService
               .getFeed(feedPath)
               .setLastTimestamp(feedService.getLastTimestamp(feedPath)); // To ensure we are really returning the latest timestamp. Not as redundant as it looks. (e.g. subfeeds)
            resume(response, feed.toJson(feedService, 1));
        } else {
            // We didn't specifically ask for Json, so we assume the user doesn't want metadata
            if (feedPath.part != null && feedPath.part.getId().isPresent()) {
                // The last part of the path is and Id, so we are asking for an individual message
                resume(response, feedService.search(feedPath));
            } else {        
                // The path is to a feed, so we are searching/listening for messages in that feed
                if (waitTime != 0) {
                    feedService.listen(feedPath, fromTime, serviceUid, waitTime, filters.getPredicates(feedService)).whenComplete((iterator, error)->{
                        if (error == null) {
                            resume(response, iterator);
                        } else {
                            response.resume(error);
                        }
                    });
                } else {
                    resume(response, feedService.search(feedPath, serviceUid, fromTime, fromInclusive, Optional.ofNullable(toTime), Optional.ofNullable(toInclusive), Optional.of(relay), filters.getPredicates(feedService)));
                }
            }
        }

        LOG.exit();
    }
    

}
