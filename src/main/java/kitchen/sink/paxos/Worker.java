package kitchen.sink.paxos;


import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Worker {
    private static Logger logger = Logger.getLogger(Worker.class.getName());


    private final int port;
    private final List<Integer> peers;
    private ExecutorService threadPool;

    public Worker(int myPort, List<Integer> peers) {
        port = myPort;
        this.peers = peers;
    }

    public void start() {
        threadPool = Executors.newSingleThreadExecutor();
        threadPool.submit(() -> {

            try {
                ServerSocket serverSocket = new ServerSocket(port);
                Socket ss = null;
                while((ss = serverSocket.accept()) != null) {
                    String serverRevd = readSocketInputStream(ss.getInputStream());
                    logger.info("Server Received : " + serverRevd);
                }
            } catch (IOException e) {
                logger.severe("Error in Reading server socket " + e.getMessage());
            }
        });
    }

    private void heartBeatToPeer(int peerPort) throws IOException {
    }

    private String readSocketInputStream(InputStream socketInput) {
        StringBuilder sb = new StringBuilder(50);


        try {
            int input;
            while ((input = socketInput.read()) > 0) {
//                byte [] chunk = new byte[1024];
//                socketInput.read(chunk);
                logger.info("Reading input " + input);
                sb.append(Character.toUpperCase(input));
//                sb.append(Character.forDigit(input, 10));
//                sb.append((byte) input);
//                sb.append(input);
            }
        } catch (IOException e) {
            logger.severe("Exception in reading server socket " + e.getMessage());
        }

        return sb.toString();
    }
}
