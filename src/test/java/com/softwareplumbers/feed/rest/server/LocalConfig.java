/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.feed.rest.server;

import com.softwareplumbers.feed.FeedService;
import com.softwareplumbers.keymanager.KeyManager;
import java.io.IOException;
import java.security.KeyStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import com.softwareplumbers.feed.rest.client.spring.FeedServiceImpl;
import com.softwareplumbers.feed.rest.client.spring.LoginHandler;
import com.softwareplumbers.feed.rest.client.spring.SignedRequestLoginHandler;
import org.springframework.context.annotation.Primary;
/**
 *
 * @author jonathan
 */
@Configuration
public class LocalConfig {
    
    @Autowired
	Environment env;

    @Bean LoginHandler loginHandler(KeyManager keyManager) throws KeyStoreException, IOException {
        SignedRequestLoginHandler handler = new SignedRequestLoginHandler();
        handler.setKeyManager(keyManager);
        handler.setAuthURI("http://localhost:8080/auth/test/service?request={request}&signature={signature}");
        handler.setRepository("test");
        return handler;
    }
    
    @Bean
    public FeedService testService(LoginHandler loginHandler) throws KeyStoreException, IOException {
        FeedServiceImpl service = new FeedServiceImpl(
            "http://localhost:8080/feed/test/",
            "http://localhost:8080/service/test/",
                loginHandler
        );
        return service;
    }
}
