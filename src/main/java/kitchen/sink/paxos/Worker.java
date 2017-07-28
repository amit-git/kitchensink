package kitchen.sink.paxos;


import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Worker {
    private static Logger logger = LoggerFactory.getLogger(Worker.class);


    private final int port;
    private final List<Integer> peers;
    private ExecutorService threadPool;

    public Worker(int myPort, List<Integer> peers) {
        port = myPort;
        this.peers = peers;
    }

    public void start() {
        threadPool = Executors.newFixedThreadPool(5);
        threadPool.submit(() -> {

            try {
                ServerSocket serverSocket = new ServerSocket(port);
                Socket ss = null;
                while((ss = serverSocket.accept()) != null) {

//                    List<String> serverRevdLines = readSocketInputStream(ss.getInputStream());
//                    logger.info("Server Received lines : " + serverRevdLines.size());
//                    serverRevdLines.stream().forEach(System.out::println);
                    final InputStream socketInput = ss.getInputStream();
                    threadPool.submit(() -> {
                        try {
                            while (true) {
                                if (checkPing(socketInput)) {
                                    Thread.sleep(5 * 1000);
                                } else {
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Exception in checking ping ", e.getMessage());
                        } finally {
                            try {
                                socketInput.close();
                            } catch (IOException e) {
                                logger.error("Exception in closing socket {}", e.getMessage());
                            }
                        }
                    });
                }
            } catch (IOException e) {
                logger.error("Error in Reading server socket " + e.getMessage());
            }
        });
    }

    private void heartBeatToPeer(int peerPort) throws IOException {
    }

    private boolean checkPing(InputStream socketInput) throws IOException {
        byte[] bytes = new byte[6];
        int bytesRead = socketInput.read(bytes);
        if (bytesRead != 1) {
            String pingStr = new String(bytes);
            String pingStrTrimmed = pingStr.trim();
            logger.info("check ping === {}", pingStrTrimmed);
            return pingStrTrimmed.equalsIgnoreCase("ping");
        }
        return false;
    }

    private List<String> readSocketInputStream(InputStream socketInput) {
        try {
            return IOUtils.readLines(socketInput);
        } catch (IOException e) {
            logger.error("Exception in reading server socket " + e.getMessage());
        }

        return Collections.EMPTY_LIST;
    }
}
