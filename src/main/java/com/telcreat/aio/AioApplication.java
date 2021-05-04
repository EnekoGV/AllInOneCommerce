package com.telcreat.aio;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class AioApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        SpringApplication.run(AioApplication.class, args);
    }

}
