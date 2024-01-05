package dk.dtu;

public class config {
    public static String SERVER_IP = null;

    public static void setIp(String ip) {
        SERVER_IP = "tcp://" + ip + "/chat?keep";
    }

    public static String getIp() {
        return SERVER_IP;
    }
}
