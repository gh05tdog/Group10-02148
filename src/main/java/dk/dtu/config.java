package dk.dtu;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class config {
    public static String SERVER_IP;

    static {
        try {
            SERVER_IP = "tcp://"+ InetAddress.getLocalHost().getHostAddress() + ":9001/chat?keep";
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setIp(String ip) {
        SERVER_IP = "tcp://" + ip + "/chat?keep";
    }

    public static String getIp() {
        return SERVER_IP;
    }
}
