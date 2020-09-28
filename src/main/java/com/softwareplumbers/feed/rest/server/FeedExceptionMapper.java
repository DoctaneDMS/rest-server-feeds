/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.feed.rest.server;

import com.softwareplumbers.feed.FeedExceptions;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 *
 * @author jonathan
 */
@Provider
public class FeedExceptionMapper implements ExceptionMapper<FeedExceptions.BaseException> {

    private static final XLogger LOG = XLoggerFactory.getXLogger(FeedExceptionMapper.class);

    @Override
    public Response toResponse(FeedExceptions.BaseException error) {
        LOG.entry(error);
        Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        switch (error.type) {
            case INVALID_PATH:
                builder = Response.status(Response.Status.NOT_FOUND);
                break;
            case INVALID_ID:
                builder = Response.status(Response.Status.NOT_FOUND);
                break;
            case INVALID_JSON:
                builder = Response.status(Response.Status.BAD_REQUEST);
                break;
            case STREAMING_EXCEPTION:
                builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
                break;
        }
        builder.entity(error.toJson());
        return LOG.exit(builder.build());        
    }    
}
