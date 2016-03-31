/**
 * Neigbor object, contains IP, corresponding Host name, and Port number.
 */
public class Neighbors {
    public String IP;
    public String Dest;
    public String Port;
    public int Router_ID;
    public boolean Found = false;

    Neighbors(String ip, String dest, String port, int id){
        this.IP = ip;
        this.Dest = dest;
        this.Port = port;
        this.Router_ID = id;
    }
}
