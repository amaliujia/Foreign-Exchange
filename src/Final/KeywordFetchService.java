package Final;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;

/**
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

    public static void main(String[] args) {
        KeywordsFetchPipeline pipeline = new KeywordsFetchPipeline();
        pipeline.setFileTopicDistributionFile("files/tutorial_test.txt");
        pipeline.setFileWordTopicDistributionFile("files/tutorial_word_test.txt");
        pipeline.setCorpusDir("corpus/");
        String[] result = pipeline.start(10, args[0]);

//        ArrayList<String[]> children= new ArrayList<String[]>();
//        for (int i = 0; i < 10; i++) {
//            children.add(pipeline.start(3, result[i]));
//        }

        // generate flare.json.
        String json = KeywordFetchService.jsonUtil(args[0], result);
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
