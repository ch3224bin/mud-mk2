package com.jefflife.mudmk2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MudMk2Application {

    public static void main(String[] args) {
        SpringApplication.run(MudMk2Application.class, args);
    }

}
