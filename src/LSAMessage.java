import java.util.ArrayList;
import java.io.Serializable;
import java.util.List;

/**
 * LSA Message .
 */
public class LSAMessage implements Serializable{
    private int age;
    private String LinkID;
    private ArrayList<Links> adv_links = new ArrayList<>();
    private String Type = null;
    private int Advertising_Id;
    private long time_created;
    private int LinkCount = 0;
    private int seqno;
    private String Faillink = null; // only for FAILURE LSA

    public LSAMessage(int aid){
        //current using the advertising router id as the linkID
        this.LinkID = Integer.toString(aid);
        this.Advertising_Id = aid;
        this.time_created = System.currentTimeMillis();
        this.Type = "LSA";
    }

    public LSAMessage(int aid, String faillink){
        this.LinkID = Integer.toString(aid);
        this.Advertising_Id = aid;
        this.time_created = System.currentTimeMillis();
        this.Faillink = faillink;
        this.Type = "FAIL_LSA";

    }

    public String getFaillink(){
        return this.Faillink;
    }

    public String getType(){
        return this.Type;
    }

    public void AddLinks (Links addone){
        this.adv_links.add(addone);
    }

    public long getTime_created(){return this.time_created;}

    public void setLinkCount(int val){
        this.LinkCount = val;
    }

    public String getLinkID(){return this.LinkID;}

    public void setSeqno(int val){
        this.seqno = val;
    }

    public int getSeqno(){return this.seqno;}

    public ArrayList<Links> getLinkArray (){return this.adv_links;}

    public int getAdvertising_Id(){return this.Advertising_Id;}

    public int getLinkCount(){return this.LinkCount;}

}
