package com.robotmq.broker.engine.handler;

import com.robotmq.broker.util.GeneralProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

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
                ///Should delete There is same socket in there/

                /// TODO : It can be problem localport on production. Change later
                Socket finalSocket = socket;
                CommonVars.SOCKET_POOL.removeIf(o -> o.getInetAddress().equals(finalSocket.getInetAddress())
                        && (o.getLocalPort() == finalSocket.getLocalPort() || o.getPort() == finalSocket.getPort()));

                HandlerThread handlerThread = new HandlerThread(socket);
                handlerThread.setName("RobotMQHandlerThread-"+socket+ LocalDateTime.now());
                handlerThread.start();
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

}
