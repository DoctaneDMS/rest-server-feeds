/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.feed.rest.server;

import com.softwareplumbers.feed.FeedPath;
import com.softwareplumbers.feed.FeedExceptions.InvalidPathSyntax;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 *
 * @author jonathan
 */
@Provider
public class Converters implements ParamConverterProvider {
    
    private static final XLogger LOG = XLoggerFactory.getXLogger(Converters.class);

    @Inject
    private javax.inject.Provider<UriInfo> uriInfoProvider;
    
    private static final ParamConverter<Instant> INSTANT_CONVERTER = new ParamConverter<Instant>() {
        @Override
        public Instant fromString(String string) {
            return string == null ? null : Instant.parse(string);
        }

        @Override
        public String toString(Instant t) {
            return t == null ? null : t.toString();
        }
            
    };
    
    private static final ParamConverter<UUID> UUID_CONVERTER = new ParamConverter<UUID>() {
        @Override
        public UUID fromString(String string) {
            return string == null ? null : UUID.fromString(string);
        }

        @Override
        public String toString(UUID t) {
            return t == null ? null : t.toString();
        }          
    };    

    private static class FeedPathConverter implements ParamConverter<FeedPath> {
        
        UriInfo uriInfo;
        
        public FeedPathConverter(UriInfo uriInfo) {
            this.uriInfo = uriInfo;
        }
        
        @Override
        public FeedPath fromString(String value) {
            LOG.entry(value);

            String format = uriInfo.getQueryParameters().getFirst("escapeWith");

            char escape = format == null ? '\\' : format.charAt(0);

            LOG.trace("Escape char is: {}", escape);

            try {
                // So, due some really, irridemably stupid design decisions WRT matrix parameters in jersey,
                // we basically have to ignore the value we've got passed in here, and re-do the
                // whole pattern matching thing so we can access the URI we are given without
                // loosing anything after the semicolon.
                String path = uriInfo.getPath();
                int start = path.indexOf("/", 1);
                start = path.indexOf("/", start+1);

                if (start > 0) {
                    value = path.substring(start, path.length());
                    LOG.trace("reconsituted value {}", value);
                    return LOG.exit(FeedPath.valueOf(value, escape));
                } else {
                    return FeedPath.ROOT;
                }                    

            } catch (InvalidPathSyntax e) {
                // FFS the jersey rules for throwing exception here are just plain pig-headed.
                // What it boils down to is that whatever we throw here will be wrapped in a NotFound 
                // exception, so is effectively uncatchable by an ExceptionMapper. 
                Response response = Response.status(Response.Status.NOT_FOUND).entity(e.toJson()).build();
                throw LOG.throwing(new WebApplicationException(response));
            } 
        }

        @Override
        public String toString(FeedPath value) {
            return value.toString();
        }
    };

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if(rawType.equals(Instant.class)){
            return (ParamConverter<T>) INSTANT_CONVERTER;
        }
        if(rawType.equals(UUID.class)){
            return (ParamConverter<T>) INSTANT_CONVERTER;
        }
        if(rawType.equals(FeedPath.class)) {
            return (ParamConverter<T>) new FeedPathConverter(uriInfoProvider.get());
        }
        return null;
    }
}