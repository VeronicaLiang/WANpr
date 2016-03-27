/**
 * Neigbor object, contains IP, corresponding Host name, and Port number.
 */
public class Neighbors {
    public static String IP;
    public static String Dest;
    public static String Port;

    Neighbors(String ip, String dest, String port){
        IP = ip;
        Dest = dest;
        Port = port;
    }
}
