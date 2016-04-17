import java.io.Serializable;

/**
 * recording links of each router.
 */
public class Links implements Serializable{
    private static final long serialVerisonUID = 1L;
    public int source;
    public int destination;
    public double cost = 0;
    public boolean active;
    public long last_update;
    private int non_active_count = 0;

    public Links(int source, int destination, double cost){
        this.source = source;
        this.destination = destination;
        this.cost = cost;
        this.last_update = System.currentTimeMillis();
    }

    public void IncreaseNonActiveCount(){
        non_active_count ++;
    }

    public int getNon_Active_Count(){
        return non_active_count;
    }

    public void resetNon_Active_Count(){
        non_active_count = 0;
    }

//    public void setActive (boolean val){
//        this.active = val;
//    }

    public String toString(){
        return source + " " + destination + " " + cost;
    }
}
