import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by amaliujia on 15-10-11.
 */
public class RandomForest {
    private static final int NUM_THREADS=Runtime.getRuntime().availableProcessors();

    // the number of category. Here should be binary.
    public static int numLabels = 2;

    // the number of attributes(features) in the data - set this before beginning the forest creation */
    public static int numAttr = 3;

    // number of random selected attributes to build binary.
    public static int numAttrRandom;

    // The collection of the forest's decision trees.
    private ArrayList<RealDecisionTree> trees;

    /** the number of trees in this random tree */
    private int numTrees;

    private ExecutorService pool;

    //The original training data matrix that will be used to generate the random forest classifier
    private ArrayList<int[]> data;

    // The data on which produced random forest will be tested
    private ArrayList<int[]> testdata;

    public RandomForest(int numTrees, ArrayList<int[]> data, ArrayList<int[]> t_data ){
        this.numTrees=numTrees;
        this.data=data;
        this.testdata=t_data;
        trees= new ArrayList<RealDecisionTree>(numTrees);
    }
    /**
     * Begins the random forest creation with multi-threading.
     */
    public void Start() {
        pool= Executors.newFixedThreadPool(NUM_THREADS);
        for (int t=0;t<numTrees;t++){
            pool.execute(new createTree(data,this,t+1));
        }

        TestForest(trees,testdata);
    }

    /**
     * Run test on the forest and print precision
     */
    private void TestForest(ArrayList<RealDecisionTree> collec_tree,ArrayList<int[]> test_data ) {
        int correctness = 0;
        int k = 0;
        ArrayList<Integer> actualValues = new ArrayList<Integer>();
        for(int[] rec : test_data){
            actualValues.add(rec[rec.length-1]);
        }

        int index = 0;
        for (int[] one_instance : test_data){
            Map<Integer, Integer> predictions = new HashMap<>();
            int res = 0;
            for (RealDecisionTree tree : collec_tree){
                int label = tree.predict(one_instance);
                if (predictions.containsKey(label)){
                    predictions.put(label, predictions.get(label) + 1);
                }else{
                    predictions.put(label, 1);
                }
            }

            if (predictions.get(0) > predictions.get(1)){
                res = 0;
            }else{
                res = 1;
            }

            if (actualValues.get(index) == res){
                correctness += 1;
            }

            index += 1;
        }

        System.out.println("Accuracy: " + (correctness / test_data.size()));
    }

    /**
     * Runnable class that build a tree.
      */
    private class createTree implements Runnable{
        /** the training data to generate the decision tree (same for all trees) */
        private ArrayList<int[]> data;
        /** the current forest */
        private RandomForest forest;
        /** the Tree number */
        private int treenum;
        /**
         * A default, dummy constructor
         */
        public createTree(ArrayList<int[]> data,RandomForest forest,int num){
            this.data=data;
            this.forest=forest;
            this.treenum=num;
        }

        public void run() {
            trees.add(new RealDecisionTree(data,forest,treenum));
        }
    }

}
