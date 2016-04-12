import java.io.Serializable;
/**
 * Packet Object.
 */
public class Packet implements Serializable{
    private static final long serialVersionUID = 5950169519310163575L;
    private String Type;
    private Object Data;
    private LSAMessage lsa;
    private String Destination;
    private int Id; //source Router's id
    private int Seqno = 0;
    private Integer crc32Code;

    public Packet (int id, String type, String dest_ip){
        this.Id = id;
        this.Type = type;
        this.Destination = dest_ip;
        this.Seqno +=1;
    }

    public Packet (int id, String type, String dest_ip, LSAMessage l){
        this.Id = id;
        this.Type = type;
        this.Destination = dest_ip;
        this.Seqno +=1;
        this.lsa = l;
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

    public void setLSAMessage(LSAMessage m){
        this.lsa = m;
    }

    public int getSeqno(){
        return Seqno;
    }

    public LSAMessage getLSA (){
        return lsa;
    }

}
