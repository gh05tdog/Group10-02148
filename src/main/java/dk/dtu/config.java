package dk.dtu;

public class config {
    public static String SERVER_IP = "tcp://localhost:9001";

    public static void setIp(String text) {
        SERVER_IP = "tcp://" + text;
    }

    public static String getIp() {
        return SERVER_IP;
    }
}
