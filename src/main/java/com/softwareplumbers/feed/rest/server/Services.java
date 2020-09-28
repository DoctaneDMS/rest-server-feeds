/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.feed.rest.server;

import com.softwareplumbers.feed.FeedService;
import com.softwareplumbers.rest.server.core.Authenticated;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author jonat
 */
@Component
@Authenticated
@Path("/service")
public class Services {

    private static final XLogger LOG = XLoggerFactory.getXLogger(Services.class);
    
    private FeedServiceFactory feedServiceFactory;
        
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
    
    @GET
    @Path("/{repository}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response info(@PathParam("repository") String repository) {
        LOG.entry(repository);
        FeedService feedService = feedServiceFactory.getService(repository);
        JsonObjectBuilder jsonResult = Json.createObjectBuilder();
        jsonResult.add("initTime", feedService.getInitTime().toString());
        jsonResult.add("serviceId", feedService.getServerId().toString());
        Response result = Response.ok(jsonResult.build()).build();
        return LOG.exit(result);        
    }           
}
