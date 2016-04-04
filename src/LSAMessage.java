import java.util.ArrayList;
import java.io.Serializable;

/**
 * LSA Message .
 */
public class LSAMessage implements Serializable{
    private int age;
    private int id = Config.ROUTER_ID;
    private ArrayList<Links> adv_links = new ArrayList<>();
    private String Type = "LSA";
    private int advertising_id;
    private long time_created;

    public LSAMessage(int aid){
        this.advertising_id = aid;
    }

    public void AddLinks (Links addone){
        this.adv_links.add(addone);
    }
}
