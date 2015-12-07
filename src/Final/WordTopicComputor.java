package Final;

import breeze.optimize.IterableOptimizationPackage;
import breeze.optimize.linear.LinearProgram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * Created by amaliujia on 15-12-5.
 */
public class WordTopicComputor {
    private String datafile;
    private Map<Integer, Long> topicStatistcs;
    private List<WordTopicSingleInstance> wordTopicSingleInstanceList;
    private Map<Integer, String> dictonary;
    private Set<Integer> topics;

    public WordTopicComputor(String file) {
        this.datafile = file;
        this.topicStatistcs = new HashMap<Integer, Long>();
        this.wordTopicSingleInstanceList = new LinkedList<WordTopicSingleInstance>();
        this.dictonary = new HashMap<Integer, String>();
        this.topics = new HashSet<Integer>();
    }

    private void colloctStatistics(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(this.datafile)));
            String cur_line = "";

            while ((cur_line = reader.readLine()) != null) {
                String[] splits = cur_line.split(" ");
                if (splits.length <= 1) {
                    continue;
                }
                WordTopicSingleInstance instance = new WordTopicSingleInstance(Integer.parseInt(splits[0]));
                this.dictonary.put(Integer.parseInt(splits[0]), splits[1]);
                for (int i = 2; i < splits.length; i++) {
                    String[] keyvalue = splits[i].split(":");
                    if (keyvalue.length != 2){
                        continue;
                    }
                    int topicID = Integer.parseInt(keyvalue[0]);
                    int wordFreq = Integer.parseInt(keyvalue[1]);
                    instance.add(topicID, wordFreq);
                    if (!this.topics.contains(topicID)) {
                        this.topics.add(topicID);
                    }
                }
                this.wordTopicSingleInstanceList.add(instance);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void computeTopics(){
        Iterator<WordTopicSingleInstance> iter = this.wordTopicSingleInstanceList.iterator();

        while (iter.hasNext()) {
            WordTopicSingleInstance instance = iter.next();
            for (int key : instance.topicStatistics.keySet()) {
                if (this.topicStatistcs.containsKey(key)) {
                    this.topicStatistcs.put(key, this.topicStatistcs.get(key) + instance.topicStatistics.get(key));
                } else {
                    this.topicStatistcs.put(key, (long)instance.topicStatistics.get(key));
                }
            }
        }
    }

    private WordTopicDistribution computeDistribution () {
        WordTopicDistribution distribution = new WordTopicDistribution();
        distribution.setDictionary(this.dictonary);
        distribution.setTopics(this.topics);
        Iterator<WordTopicSingleInstance> iter = this.wordTopicSingleInstanceList.iterator();

        while (iter.hasNext()) {
            WordTopicSingleInstance instance = iter.next();
            for (int key : instance.topicStatistics.keySet()) {
                distribution.addPair(instance.getWordId(), key,
                        instance.topicStatistics.get(key) / (double)this.topicStatistcs.get(key));
            }
        }

        return distribution;
    }

    public WordTopicDistribution compute(){
        this.colloctStatistics();
        this.computeTopics();
        return this.computeDistribution();
    }
}
