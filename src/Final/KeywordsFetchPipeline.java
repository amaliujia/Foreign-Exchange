package Final;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;

import javax.net.ssl.StandardConstants;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private String corpusDir;

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

    public void setCorpusDir(String dir) {
        this.corpusDir = dir;
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

    private void printTopNWords(int n, String ouput) throws  IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(ouput)));
        for(int i = 0; i < n; i++) {
            writer.write(this.wordTopicDistribution.getDictionary().get(this.wordsRanking.get(i).wordID) + "\n");
        }
        writer.flush();
        writer.close();
    }

    private InstanceList createInstanceList(List<String> texts) throws IOException
    {
        ArrayList<Pipe> pipes = new ArrayList<Pipe>();
        pipes.add(new CharSequence2TokenSequence());
        pipes.add(new TokenSequenceLowercase());
        pipes.add(new TokenSequenceRemoveStopwords());
        pipes.add(new TokenSequence2FeatureSequence());
        InstanceList instanceList = new InstanceList(new SerialPipes(pipes));
        instanceList.addThruPipe(new ArrayIterator(texts));
        return instanceList;
    }

    private ParallelTopicModel createLDAModel(List<String> texts, int numTopics, int numIterations) throws IOException
    {
        InstanceList instanceList = createInstanceList(texts);
        ParallelTopicModel model = new ParallelTopicModel(numTopics);
        model.addInstances(instanceList);
        model.setNumIterations(numIterations);
        model.estimate();
        return model;
    }

    private void listFilesForFolder(File folder, ArrayList<String> texts, String targetWord) throws IOException {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, texts, targetWord);
            } else {
                String text = this.readFile(fileEntry.getPath());
                String[] tokens = text.split(" ");

                for (int i = 0; i < tokens.length; i++) {
                    if (tokens[i].equals(targetWord)) {
                        texts.add(text);
                        break;
                    }
                }
            }
        }
    }


    private  void extractLDAMatrix(String targetWord, int numTopics, int numIterations, int numKeywords)  {
        File folder = new File(this.corpusDir);
        ArrayList<String> texts = new ArrayList<String>();
        ParallelTopicModel model = null;
        try {
            this.listFilesForFolder(folder, texts, targetWord);
            int numDocuments = texts.size();
             model = createLDAModel(texts,numTopics,numIterations);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private String readFile(String path)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public void start(String output) {
        try {
            this.extractLDAMatrix("");
            this.readFileTopicDistribution();
            WordTopicComputor computor = new WordTopicComputor(this.fileWordTopicDistributionFile);
            this.wordTopicDistribution = computor.compute();
            this.compute();
            Collections.sort(this.wordsRanking, Collections.reverseOrder());
            this.printTopNWords(100, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
