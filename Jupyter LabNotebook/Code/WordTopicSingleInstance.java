package Final;

import java.util.HashMap;
import java.util.Map;

/**
 * @author amaliujia
 */
public class WordTopicSingleInstance {
    private int wordId;
    // private String word;
    public Map<Integer, Integer> topicStatistics;

    public WordTopicSingleInstance (int id) {
        this.wordId = id;
        this.topicStatistics = new HashMap<Integer, Integer>();
    }

    public void add(int id, int occurence) {
        this.topicStatistics.put(id, occurence);
    }

    public int getWordId() {
        return this.wordId;
    }
}
