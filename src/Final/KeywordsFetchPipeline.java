package Final;

import java.io.*;
import java.util.*;

/**
 * Created by amaliujia on 15-12-5.
 */
public class KeywordsFetchPipeline {
    private Map<Integer, FileTopicDistribution> fileTopicDistributionList;
    private Set<Integer> fileIDSet;
    private String fileTopicDistributionFile;
    private String fileWordTopicDistributionFile;
    private WordTopicDistribution wordTopicDistribution;
    private List<RelevantWord> wordsRanking;

    public KeywordsFetchPipeline() {
        this.fileTopicDistributionList = new HashMap<Integer, FileTopicDistribution>();
        this.fileIDSet = new HashSet<Integer>();
        this.wordsRanking = new ArrayList<RelevantWord>();
    }

    public void setFileTopicDistributionFile(String filename) {
        this.fileTopicDistributionFile = filename;
    }

    public void setFileWordTopicDistributionFile(String filename) {
        this.fileWordTopicDistributionFile = filename;
    }

    private void readFileTopicDistribution() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(this.fileTopicDistributionFile)));

        String buf_line = "";
        while ((buf_line = reader.readLine()) != null) {
            String[] splits = buf_line.split("\t");
            if (splits.length <= 1) {
                continue;
            }

            FileTopicDistribution instance = new FileTopicDistribution(Integer.parseInt(splits[0]),
                    splits[1], splits.length - 2);
            this.fileIDSet.add(Integer.parseInt(splits[0]));
            for (int i = 2; i < splits.length; i++) {
                instance.addToDistribution(Double.parseDouble(splits[i]));
            }
            this.fileTopicDistributionList.put(Integer.parseInt(splits[0]), instance);
        }
    }

    private void compute() {
        Map<Integer, String> dictionary = this.wordTopicDistribution.getDictionary();
        Set<Integer> topics = this.wordTopicDistribution.getTopics();
        for (int id : dictionary.keySet()) {
            RelevantWord word = new RelevantWord();
            word.wordID = id;
            double likelihood = 0.0;
            for (int fileID : fileIDSet) {
                for (int topicID : topics) {
                    // compute the likelihood.
                    likelihood += (this.wordTopicDistribution.getPair(id, topicID) *
                                    this.fileTopicDistributionList.get(fileID).getLikelihood(topicID));
                }
            }
            word.likelihood = likelihood;
            this.wordsRanking.add(word);
        }
    }

    private void printTopNWords(int n) {
        for(int i = 0; i < n; i++) {
            System.out.println(this.wordTopicDistribution.getDictionary().get(this.wordsRanking.get(i).wordID));
        }
    }

    public void start() {
        try {
            this.readFileTopicDistribution();
            WordTopicComputor computor = new WordTopicComputor(this.fileWordTopicDistributionFile);
            this.wordTopicDistribution = computor.compute();
            this.compute();
            Collections.sort(this.wordsRanking, Collections.reverseOrder());
            this.printTopNWords(100);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
