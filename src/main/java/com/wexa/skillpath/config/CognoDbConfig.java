package com.wexa.skillpath.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CognoDbConfig {

    private static final Logger log = LoggerFactory.getLogger(CognoDbConfig.class);

    @Bean
    @Primary
    public Driver neo4jDriver(
            @Value("${cognodb.uri}") String uri,
            @Value("${cognodb.username}") String username,
            @Value("${cognodb.password}") String password) {

        Driver driver = GraphDatabase.driver(
                uri,
                AuthTokens.basic(username, password)
        );

        // IMPORTANT: do NOT let a failed connectivity check stop the app
        // from starting. If it throws here, Spring context creation fails
        // and the whole application refuses to boot -- which defeats the
        // per-request try/catch in WebController and the /health endpoint,
        // and breaks `gradle test` (contextLoads) whenever CognoDB happens
        // to be unreachable. Log and continue; every real query already
        // has its own try/catch and reports a friendly error.
        try {
            driver.verifyConnectivity();
            log.info("Connected to CognoDB at {}", uri);
        } catch (Exception e) {
            log.warn("Could not verify CognoDB connectivity at startup ({}). " +
                    "The app will still boot; check COGNODB_URI/USERNAME/PASSWORD.", e.getMessage());
        }

        return driver;
    }
}