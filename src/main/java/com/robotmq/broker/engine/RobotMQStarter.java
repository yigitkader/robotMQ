package com.robotmq.broker.engine;

import com.robotmq.broker.engine.handler.Handler;
import com.robotmq.broker.engine.handler.RobotMQHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
public class RobotMQStarter {

    private Handler robotMQHandler = RobotMQHandler.getINSTANCE();

    @PostConstruct
    void startRobotMQ() throws InterruptedException {
        robotMQHandler.handler();
    }
}
