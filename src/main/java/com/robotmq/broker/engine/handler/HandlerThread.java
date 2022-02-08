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
        log.info("New Thread Created ! Client address : {} , Port : {} , LocalPort : {}", socket.getInetAddress(), socket.getPort(),socket.getLocalPort());
    }


    @Override
    public void run() {
        log.info("Current Thread : {}",currentThread());
        try {
            inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outStream = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {

            if (!CommonVars.SOCKET_POOL.contains(this.socket)) {
                currentThread().interrupt();
                if (currentThread().isInterrupted()){
                    log.info("Thread : {} killed",currentThread());
                    break;
                }
            }

            try {

                /// INPUT
                if (inStream != null && inStream.ready()) {
                    String line = inStream.readLine();
                    if (StringUtils.hasText(line)) {

                        System.out.println(line);
                        JSONObject collect = new JSONObject(line);
                        String type = collect.getString("type");

                        if (RobotMQConstants.PRODUCE_REQUEST.equals(type)) {
                            String topic = collect.getString("topic");
                            String data = collect.getString("data");

                            BlockingQueue<String> dataOfTopic = CommonVars.TOPICS_AND_DATA.get(topic) != null
                                    ? CommonVars.TOPICS_AND_DATA.get(topic) : new LinkedBlockingQueue<>();

                            dataOfTopic.put(data);
                            CommonVars.TOPICS_AND_DATA.put(topic, dataOfTopic);

                        } else if (RobotMQConstants.SEND_TOPICS_REQUEST.equals(type)) {

                            String topics = collect.getString("topics");
                            Set<String> socketTopicsList = new HashSet<>();
                            new JSONArray(topics).forEach(o -> {
                                socketTopicsList.add(o.toString());
                            });

                            //TODO :  Only one client listen one topic. delete before if it.

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
