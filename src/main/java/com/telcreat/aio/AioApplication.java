package com.telcreat.aio;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AioApplication {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        SpringApplication.run(AioApplication.class, args);
    }

}
