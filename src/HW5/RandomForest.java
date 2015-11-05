package HW5;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author amaliujia
 */
public class RandomForest implements Serializable {
    private static final int NUM_THREADS=Runtime.getRuntime().availableProcessors();

    // the number of category. Here should be binary.
    public static int numLabels = 2;

    // the number of attributes(features) in the data - set this before beginning the forest creation */
    public static int numAttr = 3;

    // number of random selected attributes to build binary.
    public static int numAttrRandom = 1;

    // The collection of the forest's decision trees.
    private ArrayList<RealDecisionTree> trees;

    /** the number of trees in this random tree */
    private int numTrees;

    private ExecutorService pool;

    //The original training data matrix that will be used to generate the random forest classifier
    public ArrayList<int[]> data;

    // The data on which produced random forest will be tested
    public ArrayList<int[]> testdata;

    public RandomForest(ArrayList<RealDecisionTree> trees, String test_table, String[] schema){
        this.trees = trees;
        this.testdata = readDataFromCassandra(test_table, schema);

    }
    /**
     * Constructs Randomforest from Cassandra.
     * @param trainning_table
     * @param test_table
     * @param schema
     * @param numTrees
     */
    public RandomForest(String trainning_table, String test_table, String[] schema, int numTrees){
        this.numTrees = numTrees;
        this.data = readDataFromCassandra(trainning_table, schema);
        this.testdata = readDataFromCassandra(test_table, schema);
        trees= new ArrayList<RealDecisionTree>(numTrees);
    }
    /**
     * Use Cassandra driver, read data from Cassandra table given table name and table schema.
     * @param tablename
     * @param schema
     * @return
     *      List of data.
     */
    private ArrayList<int[]> readDataFromCassandra(String tablename, String[] schema){
        CassandraDriver driver = new CassandraDriver();
        return driver.queryData(tablename, schema);
    }

    public RandomForest(){

    }

    /**
     * Run test on the forest and print precision
     */
    public void TestForest(ArrayList<RealDecisionTree> collec_tree,ArrayList<int[]> test_data ) {
        int correctness = 0;
        int k = 0;
        ArrayList<Integer> actualValues = new ArrayList<Integer>();
        for(int[] rec : test_data){
            actualValues.add(rec[rec.length-1]);
        }

        int index = 0;
        for (int[] one_instance : test_data){
            Map<Integer, Integer> predictions = new HashMap<Integer, Integer>();
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
}
