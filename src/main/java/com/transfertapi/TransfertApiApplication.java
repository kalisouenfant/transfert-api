package com.transfertapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = "com.transfertapi")
public class TransfertApiApplication {

    private static final Logger logger = LoggerFactory.getLogger(TransfertApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TransfertApiApplication.class, args);
        logger.info("🚀 Application Transfert API démarrée avec succès !");
    }
}
