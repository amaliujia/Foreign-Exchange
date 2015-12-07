package Final;

/**
 * Created by amaliujia on 15-12-5.
 */
public class RelevantWord implements Comparable<RelevantWord>{
    public int wordID;
    public double likelihood;

    public int compareTo(RelevantWord o) {
        if (this.likelihood == o.likelihood) {
            return 0;
        }else if(this.likelihood < o.likelihood) {
            return -1;
        }else {
            return 1;
        }
    }
}
