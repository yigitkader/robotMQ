package com.robotmq.broker.engine.handler;

import com.robotmq.broker.vo.SocketTopics;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class HandlerThread extends Thread{


    Socket socket;
    private PrintWriter outStream = null;
    BufferedReader inStream = null;

    public HandlerThread(Socket socket) {
        this.socket = socket;
        log.info("New Reader Thread Created ! Client address : {} : {}", socket.getInetAddress(), socket.getPort());
    }


    @Override
    public void run() {

        try {
            inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outStream = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {

            if (!CommonVars.SOCKET_POOL.contains(this.socket)) {
                Thread.currentThread().interrupt();
            }

            try {

                /// INPUT
                if (inStream != null && inStream.ready()) {
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

                /// OUTPUT
                if (outStream != null) {
                    CommonVars.SOCKET_TOPICS.forEach(o -> {
                        o.getTopics().forEach(t -> {
                            final BlockingQueue<String> dataToConsumed = CommonVars.TOPICS_AND_DATA.get(t);
                            if(dataToConsumed != null){
                                dataToConsumed.forEach( d -> {
                                    outStream.println("DATA : "+d+"\n");
                                    //outStream.flush();
                                });
                                CommonVars.TOPICS_AND_DATA.remove(t);
                            }
                        });
                    });

                }


            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

}
