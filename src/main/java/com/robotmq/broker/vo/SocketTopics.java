package com.robotmq.broker.vo;

import java.net.Socket;
import java.util.Set;

/**
 * @author yigitkader
 */
public class SocketTopics {

    private Socket socket;
    private Set<String> topics;

    public SocketTopics(Socket socket, Set<String> topics) {
        this.socket = socket;
        this.topics = topics;
    }

    public Set<String> getTopics() {
        return topics;
    }

    public void setTopics(Set<String> topics) {
        this.topics = topics;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
