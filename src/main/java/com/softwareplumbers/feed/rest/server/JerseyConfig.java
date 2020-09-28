package com.softwareplumbers.feed.rest.server;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.stereotype.Component;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

import com.softwareplumbers.rest.server.core.*;

@Component
public class JerseyConfig extends ResourceConfig {

	public JerseyConfig() {
		property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
		register(Heartbeat.class);
		register(Authentication.class);
		register(CORSRequestFilter.class);
        register(CORSResponseFilter.class);
	    register(MultiPartFeature.class);
	    register(AuthenticationFilter.class);
        register(Feeds.class);
        register(Services.class);
        register(Host.class);
        register(Converters.class);
        register(FeedExceptionMapper.class);
	}
}