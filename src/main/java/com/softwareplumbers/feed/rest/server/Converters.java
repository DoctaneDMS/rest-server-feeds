/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.feed.rest.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.UUID;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author jonathan
 */
@Provider
public class Converters implements ParamConverterProvider {
    
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

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if(rawType.equals(Instant.class)){
            return (ParamConverter<T>) INSTANT_CONVERTER;
        }
        if(rawType.equals(UUID.class)){
            return (ParamConverter<T>) INSTANT_CONVERTER;
        }
        return null;
    }
}