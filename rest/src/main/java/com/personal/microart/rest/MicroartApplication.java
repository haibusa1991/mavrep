package com.personal.microart.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.personal.microart")
@EnableJpaRepositories(basePackages = "com.personal.microart.persistence.repositories")
@EntityScan(basePackages = "com.personal.microart.persistence.entities")
@EnableCaching
@EnableScheduling
public class MicroartApplication {

    //TODO: limit the number of requests to the server - max 100 requests per minute
    public static void main(String[] args) {
        SpringApplication.run(MicroartApplication.class, args);
    }

}
