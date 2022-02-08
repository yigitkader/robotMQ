package com.robotmq.broker.engine.handler;

import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;

public class CommonVars {

    public static volatile Map<String, Set<Socket>> topicSocketPool = new ConcurrentHashMap<>();
}
