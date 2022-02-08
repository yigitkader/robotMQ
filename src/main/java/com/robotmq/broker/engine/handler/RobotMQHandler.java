package com.robotmq.broker.engine.handler;

import com.robotmq.broker.util.GeneralProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Component
@Qualifier("robotMQHandler")
public class RobotMQHandler implements Handler{

    private final static String ROBOTMQ_SERVER_SOCKET_PORT = GeneralProperties.getINSTANCE().getPropertyValue("application.properties","robotmq.serversocket.listen.port");

    private ServerSocket robotMQServerSocket;


    @Override
    public void handler() throws InterruptedException {
        Socket socket = null;
        robotMQServerSocket = createServerSocket();
        while (true) {
            try {
                socket = robotMQServerSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            if (socket != null){
                new ReaderHandlerThread(socket).start();
                new WriterHandlerThread(socket).start();
                CommonVars.SOCKET_POOL.add(socket);
            }
        }
    }

    public ServerSocket createServerSocket() throws InterruptedException {
        try {
            robotMQServerSocket = new ServerSocket(Integer.parseInt(ROBOTMQ_SERVER_SOCKET_PORT));
            System.out.println("Created Server Socket");
        } catch (IOException e) {
            e.printStackTrace();
            throw new InterruptedException();
        }
        return robotMQServerSocket;
    }



    // todo : Complete here
    void heartBeat(){
        //Del socket from socketpool if connection close
    }
}
