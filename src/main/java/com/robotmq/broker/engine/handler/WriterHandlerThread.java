package com.robotmq.broker.engine.handler;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class WriterHandlerThread extends Thread {

    Socket socket;

    private PrintWriter outStream = null;

    public WriterHandlerThread(Socket socket) {
        this.socket = socket;
        log.info("New Writer Thread Created ! Client address : {} : {}",socket.getInetAddress(),socket.getPort());
    }



    @Override
    public void run() {

        try {
             outStream = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            if(!CommonVars.SOCKET_POOL.contains(this.socket)){
                Thread.currentThread().interrupt();
            }
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

        }
    }
}
