package Final;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by amaliujia on 15-12-5.
 */
public class WordTopicDistribution {
    private Map<Pair<Integer, Integer>, Double> wordTopicDistributionMap;
    private Map<Integer, String> dictionary;
    private Set<Integer> topics;

    public WordTopicDistribution() {
        this.wordTopicDistributionMap = new HashMap<Pair<Integer, Integer>, Double>();
    }

    public void addPair(int wordID, int topicID, double probability) {
        this.wordTopicDistributionMap.put(new Pair<Integer, Integer>(topicID, wordID), probability);
    }

    public double getPair(int wordID, int topicID) {
        Pair<Integer, Integer> key = new Pair<Integer, Integer>(topicID, wordID);
        if (this.wordTopicDistributionMap.containsKey(key)) {
            return this.wordTopicDistributionMap.get(key);
        } else {
            return 0;
        }
    }

    public void setDictionary(Map<Integer, String> d) {
        this.dictionary = d;
    }

    public Map<Integer, String> getDictionary() {
        return this.dictionary;
    }

    public void setTopics(Set<Integer> t) {
        this.topics = t;
    }

    public Set<Integer> getTopics() {
        return this.topics;
    }

}
