package com.mulakatim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MulakatimApplication {
    public static void main(String[] args) {
        SpringApplication.run(MulakatimApplication.class, args);
    }
}
