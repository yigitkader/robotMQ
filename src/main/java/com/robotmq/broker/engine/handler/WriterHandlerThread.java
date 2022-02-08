package com.robotmq.broker.engine.handler;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class WriterHandlerThread extends Thread {

    Socket socket;

    public WriterHandlerThread(Socket socket) {
        this.socket = socket;
        log.info("New Writer Thread Created ! Client address : {} : {}",socket.getInetAddress(),socket.getPort());
    }


    @Override
    public void run() {

        PrintWriter outStream = null;

        try {
            outStream = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            if (outStream != null) {
                if (!CommonVars.topicSocketPool.isEmpty()) {

                }

                outStream.println("ahey ahey from server \n");
                outStream.flush();
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
