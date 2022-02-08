package com.robotmq.broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RobotmqApplication {

    public static void main(String[] args) {
        SpringApplication.run(RobotmqApplication.class, args);
    }

}
