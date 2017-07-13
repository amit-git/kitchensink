package kitchen.sink.paxos;

import java.util.Arrays;
import java.util.logging.Logger;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Worker w = new Worker(5001, Arrays.asList(5002, 5003, 5004));
        w.start();
    }
}
