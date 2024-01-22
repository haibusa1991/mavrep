package com.personal.mavrep.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.personal.mavrep")
@EnableJpaRepositories(basePackages = "com.personal.mavrep.persistence.repositories")
@EntityScan(basePackages = "com.personal.mavrep.persistence.entities")
@EnableCaching
@EnableScheduling
public class MavrepApplication {

    public static void main(String[] args) {
        SpringApplication.run(MavrepApplication.class, args);
    }

}
