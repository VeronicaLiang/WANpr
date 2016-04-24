import java.io.Serializable;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Object for transferring files.
 */
public class DataMessage implements Serializable{
    private static final long serialVersionUID = 5950169519310163575L;

    private int SenderId; // the sending router's id
    private int RecvId; // the destination receiver's id
    private byte [] Data; // contains part of the file
    private int Seqno; // indicates the order
    private String Type; // indicates whether it is syn, data or fin
    private String Crossed_Path = Config.ROUTER_ID+"";
    private long crc32code;
    private int MaxHop = sLSRP.graph.size();

    public DataMessage(int s, int r, byte [] d, int seq, long c, String t){
        this.SenderId = s;
        this.RecvId = r;
        this.Data = d;
        this.Seqno = seq;
        this.crc32code = c;
        this.Type = t;
    }

    public int getRecvId(){
        return this.RecvId;
    }

    public int getSenderId(){
        return this.SenderId;
    }

    public int getSeqno(){
        return this.Seqno;
    }

    public void appendPath(String a){
        this.Crossed_Path = this.Crossed_Path + a;
    }

    public String getCrossed_Path(){
        return this.Crossed_Path;
    }

    public void setPath(String b){
        this.Crossed_Path = b;
    }

    public void setData(byte [] content){
        this.Data = content;
    }

    public byte [] getData(){
        return this.Data;
    }

    public long getCrc32code(){
        return crc32code;
    }

    public void setCrossed_Path(String p){
        this.Crossed_Path = p;
    }

    public int getMaxHop(){
        return this.MaxHop;
    }

    public void decreaseMaxHop(){
        this.MaxHop--;
    }

}
