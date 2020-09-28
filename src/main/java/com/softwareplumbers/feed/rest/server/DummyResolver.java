/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.feed.rest.server;

import com.softwareplumbers.feed.Cluster;
import com.softwareplumbers.feed.impl.Resolver;
import java.net.URI;
import java.util.Optional;
import javax.json.JsonObject;

/**
 *
 * @author jonat
 */
public class DummyResolver implements Resolver<Cluster.Host> {

    @Override
    public Optional<Cluster.Host> resolve(URI uri, JsonObject jo) {
        return Optional.empty();
    } 
}
