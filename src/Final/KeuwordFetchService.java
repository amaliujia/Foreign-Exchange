package Final;

/**
 * @author amaliujia
 */
public class KeuwordFetchService {
    public static void main(String[] args) {
        KeywordsFetchPipeline pipeline = new KeywordsFetchPipeline();
        pipeline.setFileTopicDistributionFile("files/tutorial_composition.txt");
        pipeline.start();
    }
}
