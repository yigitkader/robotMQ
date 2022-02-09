package com.robotmq.broker.engine;

import com.robotmq.broker.vo.SocketTopics;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


public class CommonVars {

    public static volatile Set<Socket> SOCKET_POOL = ConcurrentHashMap.newKeySet();

    public static volatile Map<String, BlockingQueue<String>> TOPICS_AND_DATA = new ConcurrentHashMap<>();

    public static volatile Set<SocketTopics> SOCKET_TOPICS = ConcurrentHashMap.newKeySet();

}
