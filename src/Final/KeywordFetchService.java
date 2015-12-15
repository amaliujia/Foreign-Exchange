package Final;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


/**
 * This class fetch two levels words for target word. The first level contains 10 words for target word.
 * The second level contains 4 words for each level one word. The output of these two levels is a JSON file.
 * @author amaliujia
 */
public class KeywordFetchService {

    public static String jsonPairUtil(String word) {
       return "{\"name\": \"" + word + "\", \"size\": 1}";
    }
    public static String jsonUtil(String word, String[] childen) {
        String result = "{\n\"name\": \"";
        result += word;
        result += "\",\n";
        result += "\"children\":[\n";
        for (int i = 0; i < childen.length-1; i++) {
            result += KeywordFetchService.jsonPairUtil(childen[i]);
            result += ",\n";
        }
        result += KeywordFetchService.jsonPairUtil(childen[childen.length-1]);
        result += "\n";

        result += "]\n}";
        return result;
    }
    public static String jsonPUtil(String word, String[] childen, ArrayList<String[]> childrens) {
        String result = "{\n\"name\": \"";
        result += word;
        result += "\",\n";
        result += "\"children\":[\n";
        for (int i = 0; i < childen.length-1; i++) {
            result += KeywordFetchService.jsonUtil(childen[i], childrens.get(i));
            result += ",\n";
        }
        result += KeywordFetchService.jsonUtil(childen[childen.length-1], childrens.get(childen.length-1));
        result += "\n";

        result += "]\n}";
        return result;
    }

    public static void main(String[] args) {
        KeywordsFetchPipeline pipeline = new KeywordsFetchPipeline();
        pipeline.setFileTopicDistributionFile("LabNotebook/Data/Document_Topic_Distribution/tutorial_composition_AB.txt");
        pipeline.setFileWordTopicDistributionFile("LabNotebook/Data/Word_Topic_Counts/tutorial_word_topic_AB.txt");
        pipeline.setCorpusDir("LabNotebook/Data/Corpus");
        String[] result = pipeline.start(10, args[0]);

        ArrayList<String[]> children= new ArrayList<String[]>();
        for (int i = 0; i < result.length; i++) {
            pipeline = new KeywordsFetchPipeline();
            pipeline.setFileTopicDistributionFile("LabNotebook/Data/Document_Topic_Distribution/tutorial_composition_AB.txt");
            pipeline.setFileWordTopicDistributionFile("LabNotebook/Data/Word_Topic_Counts/tutorial_word_topic_AB.txt");
            pipeline.setCorpusDir("LabNotebook/Data/Corpus");
            children.add(pipeline.start(4, result[i]));
        }

        // generate flare.json.
        String json = KeywordFetchService.jsonPUtil(args[0], result, children);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File("d3/flare.json")));
            writer.write(json);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
