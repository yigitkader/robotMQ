package com.robotmq.broker.engine.handler;

import com.robotmq.broker.engine.CommonVars;
import com.robotmq.broker.util.RobotMQConstants;
import com.robotmq.broker.vo.SocketTopics;

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
import java.util.logging.Logger;

/**
 * @author yigitkader
 * @implNote Worker Thread for each client
 */
public class HandlerThread extends Thread {

    private final Logger log = Logger.getLogger(HandlerThread.class.getName());

    private final Socket socket;
    private PrintWriter outStream = null;
    private BufferedReader inStream = null;

    public HandlerThread(Socket socket) {
        this.socket = socket;
        log.info("New Thread Created ! "
                + "Client address : " + socket.getInetAddress()
                + " , Port : " + socket.getPort()
                + " , LocalPort : " + socket.getLocalPort());
    }

    @Override
    public void run() {
        log.info("Thread : " + currentThread() + " started");
        try {
            inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outStream = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            if (!CommonVars.SOCKET_POOL.contains(this.socket)) {
                try {
                    currentThread().interrupt();
                    if (currentThread().isInterrupted()) {
                        log.warning("Thread : " + currentThread() + " killed");
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                if (inStream != null && inStream.ready()) {
                    handleInputStream(inStream); /// Input Streaming Operations
                }

                if (outStream != null) {
                    handleOutputStream(); /// Output Streaming Operations
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void handleInputStream(BufferedReader inStream) throws InterruptedException, IOException {
        String line = inStream.readLine();
        System.out.println(line);
        if (StringUtils.hasText(line)) {

            JSONObject collectionFromClient = new JSONObject(line);
            String requestType = collectionFromClient.getString("type");

            if (RobotMQConstants.PRODUCE_REQUEST.equals(requestType)) {
                String topic = collectionFromClient.getString("topic");
                String data = collectionFromClient.getString("data");

                BlockingQueue<String> dataQueueOfTopic = CommonVars.TOPICS_AND_DATA.get(topic) != null
                        ? CommonVars.TOPICS_AND_DATA.get(topic) : new LinkedBlockingQueue<>();

                dataQueueOfTopic.put(data);
                CommonVars.TOPICS_AND_DATA.put(topic, dataQueueOfTopic);

            } else if (RobotMQConstants.SAVE_TOPICS_WILL_CONSUME_REQUEST.equals(requestType)) {

                String topics = collectionFromClient.getString("topics");
                Set<String> socketTopicsList = new HashSet<>();

                new JSONArray(topics).forEach(o -> socketTopicsList.add(o.toString()));

                SocketTopics socketTopics = new SocketTopics(this.socket, socketTopicsList);

                CommonVars.SOCKET_TOPICS.add(socketTopics);

            }
        }

    }

    private void handleOutputStream() {
        JSONObject jsonObjectToSendToClient = new JSONObject();

        CommonVars.SOCKET_TOPICS.forEach(o -> o.getTopics().forEach(t -> {
            final BlockingQueue<String> dataToConsumed = CommonVars.TOPICS_AND_DATA.get(t);
            if (dataToConsumed != null) {
                dataToConsumed.forEach(d -> {
                    try {
                        jsonObjectToSendToClient.put("topic", t);
                        jsonObjectToSendToClient.put("data", d);
                        outStream.println(jsonObjectToSendToClient + "\n\r");
                        outStream.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                CommonVars.TOPICS_AND_DATA.remove(t);
                //TODO :  Only one client listen one topic. delete before if it.
            }
        }));

    }

}
