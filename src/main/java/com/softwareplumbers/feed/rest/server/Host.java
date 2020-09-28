/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.feed.rest.server;

import com.softwareplumbers.feed.Cluster;
import com.softwareplumbers.feed.FeedService;
import com.softwareplumbers.rest.server.core.Authenticated;
import java.util.UUID;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
@Path("/host")
public class Host {

    private static final XLogger LOG = XLoggerFactory.getXLogger(Host.class);
    
    private Cluster cluster;
        
    /**
     * Use by Spring to inject a service factory for retrieval of a cluster service.
     * 
     * @param serviceFactory A factory for retrieving the cluster service
     */
    @Autowired
    public void setCluster(Cluster cluster) {
        LOG.entry(cluster);
        this.cluster = cluster;
    }
    
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getLocalServices() {
        LOG.entry();
        JsonArrayBuilder jsonResult = Json.createArrayBuilder();
        Cluster.LocalHost localhost = cluster.getLocalHost();
        localhost.getLocalServices().forEach(service -> {
            jsonResult.add(service.getServerId().toString());
        });
        Response result = Response.ok(jsonResult.build()).build();
        return LOG.exit(result);        
    }
    
    @PUT
    @Path("replicators/{from}/{to}")
    public Response replicate(
        @PathParam("to") UUID to, 
        @PathParam("from") UUID from
    ) {
        LOG.entry(to, from);
        Cluster.LocalHost localhost = cluster.getLocalHost();
        localhost.replicate(to, from);
        return LOG.exit(Response.accepted().build());                
    };
    
    @DELETE
    @Path("replicators")
    public Response closeReplication(
        @QueryParam("serviceId") UUID serviceId
    ) {
        LOG.entry(serviceId);
        Cluster.LocalHost localhost = cluster.getLocalHost();
        localhost.closeReplication(serviceId);
        return LOG.exit(Response.accepted().build());                
    };
}
