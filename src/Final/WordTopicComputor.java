package Final;

import breeze.optimize.IterableOptimizationPackage;

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

    public WordTopicComputor(String file) {
        this.datafile = file;
        this.topicStatistcs = new HashMap<Integer, Long>();
        this.wordTopicSingleInstanceList = new LinkedList<WordTopicSingleInstance>();
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
                WordTopicSingleInstance instance = new WordTopicSingleInstance(Integer.parseInt(splits[0]), splits[1]);
                for (int i = 2; i < splits.length; i++) {
                    String[] keyvalue = splits[i].split(":");
                    if (keyvalue.length != 2){
                        continue;
                    }
                    instance.add(Integer.parseInt(keyvalue[0]), Integer.parseInt(keyvalue[1]));
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

        }
    }

    public WordTopicDistribution compute(){
        this.colloctStatistics();
        return null;
    }
}
