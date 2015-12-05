package Final;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by amaliujia on 15-12-5.
 */
public class KeywordsFetchPipeline {
    private List<FileTopicDistribution> fileTopicDistributionList;
    private Set<Integer> fileIDSet;
    private String fileTopicDistributionFile;
    private String fileWordTopicDistributionFile;
    private WordTopicDistribution wordTopicDistribution;

    public KeywordsFetchPipeline() {
        this.fileTopicDistributionList = new ArrayList<FileTopicDistribution>();
        this.fileIDSet = new HashSet<Integer>();
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
        }
    }


    public void start() {
        try {
            this.readFileTopicDistribution();
            WordTopicComputor computor = new WordTopicComputor(this.fileWordTopicDistributionFile);
            this.wordTopicDistribution = computor.compute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
