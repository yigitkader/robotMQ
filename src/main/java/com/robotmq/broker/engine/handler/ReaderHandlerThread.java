package com.robotmq.broker.engine.handler;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

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
                String line = inStream.readLine();
                if (line != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

}
