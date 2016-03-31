import java.io.Serializable;
/**
 * Packet Object.
 */
public class Packet implements Serializable{
    private static final long serialVersionUID = 5950169519310163575L;
    public String Type;
    public Object Data;
    public String Destination;
    public int Id; //source Router's id

    public Packet (int id, String type, String dest_ip){
        this.Id = id;
        this.Type = type;
        this.Destination = dest_ip;
    }

    public void setID(int id){
        this.Id = id;
    }

    public int getId(){
        return Id;
    }

    public void setType(String type){
        this.Type = type;
    }

    public String getType(){
        return Type;
    }

    public void setDestination(String ip){
        this.Destination = ip;
    }

    public String getDestination(){
        return Destination;
    }
}
