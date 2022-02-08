package com.robotmq.broker.engine.handler;

import com.robotmq.broker.vo.SocketTopics;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static org.reflections.util.ConfigurationBuilder.build;

@Slf4j
public class ReaderHandlerThread extends Thread {

    Socket socket;

    public ReaderHandlerThread(Socket socket) {
        this.socket = socket;
        log.info("New Reader Thread Created ! Client address : {} : {}", socket.getInetAddress(), socket.getPort());
    }


    @Override
    public void run() {

        BufferedReader inStream = null;

        try {
            inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {

            if (!CommonVars.SOCKET_POOL.contains(this.socket)) {
                Thread.currentThread().interrupt();
            }

            try {
                if (inStream != null) {
                    String line = inStream.readLine();
                    if (StringUtils.hasText(line)) {

                        System.out.println(line);
                        JSONObject collect = new JSONObject(line);
                        String type = collect.getString("type");

                        if ("produce-request".equals(type)) {
                            String topic = collect.getString("topic");
                            String data = collect.getString("data");

                            BlockingQueue<String> dataOfTopic = CommonVars.TOPICS_AND_DATA.get(topic) != null
                                    ? CommonVars.TOPICS_AND_DATA.get(topic) : new LinkedBlockingQueue<>();

                            dataOfTopic.put(data);
                            CommonVars.TOPICS_AND_DATA.put(topic, dataOfTopic);

                        } else if ("send-topics-request".equals(type)) {

                            String topics = collect.getString("topics");
                            Set<String> socketTopicsList = new HashSet<>();
                            new JSONArray(topics).forEach(o -> {
                                socketTopicsList.add(o.toString());
                            });

                            SocketTopics socketTopics = SocketTopics.builder()
                                    .socket(this.socket)
                                    .topics(socketTopicsList)
                                    .build();
                            CommonVars.SOCKET_TOPICS.add(socketTopics);
                        }


                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

}
