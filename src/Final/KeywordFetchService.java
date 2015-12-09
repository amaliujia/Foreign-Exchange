package Final;

/**
 * @author amaliujia
 */
public class KeywordFetchService {
    public static void main(String[] args) {
        KeywordsFetchPipeline pipeline = new KeywordsFetchPipeline();
        pipeline.setFileTopicDistributionFile("files/tutorial_composition.txt");
        pipeline.setFileWordTopicDistributionFile("files/tutorial_word_topic.txt");
        pipeline.start("files/ranking.txt");
    }
}
