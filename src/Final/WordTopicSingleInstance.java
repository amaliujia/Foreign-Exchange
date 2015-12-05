package Final;

import org.apache.commons.beanutils.converters.IntegerArrayConverter;
import org.apache.commons.collections.map.HashedMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by amaliujia on 15-12-5.
 */
public class WordTopicSingleInstance {
    private int wordId;
    private String word;
    private Map<Integer, Integer> topicStatistics;

    public WordTopicSingleInstance (int id, String w) {
        this.wordId = id;
        this.word = w;
        this.topicStatistics = new HashMap<Integer, Integer>();
    }

    public void add(int id, int occurence) {
        this.topicStatistics.put(id, occurence);
    }
}
