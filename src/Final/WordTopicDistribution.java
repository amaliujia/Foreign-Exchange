package Final;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by amaliujia on 15-12-5.
 */
public class WordTopicDistribution {
    private Map<Pair<Integer, Integer>, Double> wordTopicDistributionMap;

    public WordTopicDistribution() {
        this.wordTopicDistributionMap = new HashMap<Pair<Integer, Integer>, Double>();
    }

    public void addPair(int wordID, int topicID, double probability) {
        this.wordTopicDistributionMap.put(new Pair<Integer, Integer>(topicID, wordID), probability);
    }
}
