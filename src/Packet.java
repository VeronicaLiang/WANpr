import java.io.Serializable;
/**
 * Packet Object.
 */
public class Packet implements Serializable{
    private static final long serialVersionUID = 5950169519310163575L;
    private String Type;
    private LSAMessage lsa;
    private String Destination;
    private int Id; //source Router's id
    private int Seqno;
    private Integer crc32Code;
    private DataMessage Data;

    public Packet (int id, String type, String dest_ip, int seq){
        this.Id = id;
        this.Type = type;
        this.Destination = dest_ip;
        this.Seqno = seq;
    }

    public Packet (int id, String type, String dest_ip, LSAMessage l){
        this.Id = id;
        this.Type = type;
        this.Destination = dest_ip;
        this.Seqno = l.getSeqno();
        this.lsa = l;
    }

    public Packet (int id, String type, String dest_ip, DataMessage d){
        this.Id = id;
        this.Type = type;
        this.Destination = dest_ip;
        this.Data = d;
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

    public void setSeqno(int number){ this.Seqno = number;}

    public LSAMessage getLSA (){
        return lsa;
    }

    public DataMessage getFileMessage (){
        return this.Data;
    }

}
