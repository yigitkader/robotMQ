package com.robotmq.broker.engine.handler;

import com.robotmq.broker.engine.CommonVars;
import com.robotmq.broker.util.GeneralProperties;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * @author yigitkader
 */
public class RobotMQHandler implements Handler {

    private static RobotMQHandler INSTANCE = new RobotMQHandler();

    private final static String ROBOTMQ_SERVER_SOCKET_PORT = GeneralProperties.getINSTANCE()
            .getPropertyValue("application.properties", "robotmq.serversocket.listen.port");

    private final Logger logger = Logger.getLogger(RobotMQHandler.class.getName());

    private ServerSocket robotMQServerSocket;


    private RobotMQHandler() {
    }


    public static RobotMQHandler getINSTANCE() {
        return INSTANCE;
    }

    @Override
    public void handler() throws InterruptedException {
        Socket socket = null;
        robotMQServerSocket = createServerSocket();
        logger.info("RobotMQ Started");
        while (true) {
            try {
                socket = robotMQServerSocket.accept();
            } catch (IOException e) {
                logger.severe("I/O error: " + e);
            }
            if (socket != null) {
                /// TODO : It can be problem localport on production. Change later
                Socket finalSocket = socket;
                CommonVars.SOCKET_POOL.removeIf(o -> o.getInetAddress().equals(finalSocket.getInetAddress())
                        && (o.getLocalPort() == finalSocket.getLocalPort() || o.getPort() == finalSocket.getPort()));

                HandlerThread handlerThread = new HandlerThread(socket);
                handlerThread.setName("RobotMQHandlerThread-" + socket + LocalDateTime.now());
                handlerThread.start();
                CommonVars.SOCKET_POOL.add(socket);
            }
        }
    }

    public ServerSocket createServerSocket() throws InterruptedException {
        try {
            robotMQServerSocket = new ServerSocket(Integer.parseInt(ROBOTMQ_SERVER_SOCKET_PORT));
            logger.info("Created Server Socket");
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Can Not Created RobotMQ Server Socket");
            throw new InterruptedException();
        }
        return robotMQServerSocket;
    }

}
