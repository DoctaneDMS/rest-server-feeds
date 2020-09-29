/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.feed.rest.server;

import com.softwareplumbers.feed.FeedService;
import com.softwareplumbers.feed.Filters;
import com.softwareplumbers.feed.Message;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.function.Predicate;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;

/**
 *
 * @author jonat
 */
public class RemoteFilters {
    JsonArray predicates;
    
    Predicate<Message>[] getPredicates(FeedService service) { return Filters.using(service).fromJson(predicates); }
    
    public RemoteFilters(String encodedFilters) throws IOException {
        if (encodedFilters.length() == 0) {
            predicates = JsonArray.EMPTY_JSON_ARRAY;
        } else {
            try (InputStream is = new ByteArrayInputStream(Base64.getUrlDecoder().decode(encodedFilters)); JsonReader reader = Json.createReader(is)) {
                predicates = reader.readArray();
            } 
        }
    }
    
    public String toString() {
        return predicates.toString();
    }
}
