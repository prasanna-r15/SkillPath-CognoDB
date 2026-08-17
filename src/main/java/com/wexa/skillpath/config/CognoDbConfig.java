package com.wexa.skillpath.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CognoDbConfig {

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

        // Temporary connectivity verification
        driver.verifyConnectivity();

        System.out.println("========================================");
        System.out.println("CognoDB connection successful");
        System.out.println("URI: " + uri);
        System.out.println("Username: " + username);
        System.out.println("========================================");

        return driver;
    }
}