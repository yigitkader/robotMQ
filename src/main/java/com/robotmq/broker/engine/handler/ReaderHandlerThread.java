package com.robotmq.broker.engine.handler;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingDeque;

@Slf4j
public class ReaderHandlerThread extends Thread {

    Socket socket;

    public ReaderHandlerThread(Socket socket) {
        this.socket = socket;
        log.info("New Reader Thread Created ! Client address : {} : {}",socket.getInetAddress(),socket.getPort());
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
            try {
                if (inStream != null){
                    String line = inStream.readLine();
                    if (line != null) {
                        System.out.println(line);
                        JSONObject data = new JSONObject(line);
                        String type = data.getString("type");
                        if ("produce-request".equals(type)){
                            //send to produce queue
                        }else if("send-topics-request".equals(type)){
                            // Topics for compare consume operations
                            String topics = data.getString("topics");
                            JSONArray topicsJsonArray = new JSONArray(topics);
                            topicsJsonArray.forEach(t -> {
                                Set<Socket> sockets = CommonVars.topicSocketPool.get(t.toString());
                                if (sockets == null){
                                    Set<Socket> willBeAddedSocket = new LinkedHashSet<>();
                                    willBeAddedSocket.add(this.socket);
                                    CommonVars.topicSocketPool.put(t.toString(),willBeAddedSocket);
                                }else {
                                    sockets.add(this.socket);
                                    CommonVars.topicSocketPool.put(t.toString(),sockets);
                                }
                            });
                        }


                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

}
