package com.robotmq.broker.engine;

import com.robotmq.broker.engine.handler.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
public class RobotMQStarter {

    @Autowired
    @Qualifier("robotMQHandler")
    private Handler robotMQHandler;

    @PostConstruct
    void startRobotMQ() throws InterruptedException {
        robotMQHandler.handler();
    }
}
