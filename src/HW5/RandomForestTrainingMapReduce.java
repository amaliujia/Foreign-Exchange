package HW5;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.*;
import java.util.ArrayList;


/**
 * @author amaliujia
 */
public class RandomForestTrainingMapReduce {
    public static int N = 3;
    public static class DummyMapper
            extends Mapper<Object, Text, Text, IntWritable>{

        /**
         * Setup function of map task, call once.
         * Used to train a tree based on randomly selected attributes.
         */
        private void setup(){
            buildTree(2);
        }
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            return;
        }

        /**
         * In each mapper, call build tree function to train a tree. The data reads from Cassandra.
         * @param random_attr
         *          The random attr used for trainning the tree.
         */
        private void buildTree(int random_attr){
            String[] schema = new String[]{"min, max, avg, label"};
            CassandraDriver driver = CassandraDriver.getInstance();
            ArrayList<int[]> train = driver.queryData("data", schema);
            RandomForest forest = new RandomForest();
            forest.numAttr = RandomForestTrainingMapReduce.N;
            forest.numAttrRandom = random_attr;
            RealDecisionTree tree = new RealDecisionTree(train, forest, RandomForestTrainingMapReduce.N);

            // serialize this tree and write to Cassandra.
            byte[] t = serialize(tree);
            try {
                driver.insertData(t, "Forest");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Util function used to serialize a tree.
         * @param object
         *          Instance os a RealDecisionTree
         * @return
         *          bytes of the tree.
         */
        private byte[] serialize(RealDecisionTree object){
            try{
                // Serialize data object to a file
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("MyObject.ser"));
                out.writeObject(object);
                out.close();

                // Serialize data object to a byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
                out = new ObjectOutputStream(bos) ;
                out.writeObject(object);
                out.close();

                // Get the bytes of the serialized object
                byte[] buf = bos.toByteArray();
                return buf;
            } catch (IOException e) {
                System.exit(1);
            }

            return null;
        }
    }

    public static class ForestTestingReducer
            extends Reducer<Text,IntWritable,Text,IntWritable> {
        /**
         * The aggregation of Forest happens in setup phrase of Reducer. This function executes only once, so this is
         * a good place I read trees from database and aggregate them to a forest, then does testing.
         */
        public void setup(){
            CassandraDriver driver = CassandraDriver.getInstance();
            ArrayList<byte[]> arr = driver.queryData("Forest", "tree");

            ArrayList<RealDecisionTree> trees = new ArrayList<RealDecisionTree>();
            for (byte[] b : arr){
                trees.add(deserialize(b));
            }
            RandomForest forest = new RandomForest(trees, "test", new String[]{"min, mav, avg, label"});
            forest.TestForest(trees, forest.testdata);
        }

        /**
         * Don't use reduce function.
         * @param key
         * @param values
         * @param context
         * @throws IOException
         * @throws InterruptedException
         */
        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            return;
        }

        /**
         * Read bytes from Cassandra and cast to RealdecisionTreeType.
         * @param bytes
         * @return
         */
        private RealDecisionTree deserialize(byte[] bytes){
            try {
                ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes));
                RealDecisionTree tree = (RealDecisionTree)stream.readObject();
                return tree;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    /**
     * This is the driver before mapper and reduer run. Driver only reads dummy data from hdfs, which is used to
     * control the number of map tasks spawned. Typical the number of input splits decide the number of map tasks.
     */
    public static class TrainingDriver extends Configured implements Tool{
        public int run(String[] strings) throws Exception {
            Job job = new Job(getConf(), "Ramdom Forest Traning");
            job.setJarByClass(RandomForestTrainingMapReduce.class);
            job.setMapperClass(DummyMapper.class);
            job.setReducerClass(ForestTestingReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);
            // only need one reducer for aggregating.
            job.setNumReduceTasks(1);
            FileInputFormat.addInputPath(job, new Path(strings[0]));
            FileOutputFormat.setOutputPath(job, new Path(strings[1]));
            System.exit(job.waitForCompletion(true) ? 0 : 1);
            return 0;
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: Random Forest <in> <out>");
            System.exit(2);
        }
        System.exit(ToolRunner.run(conf, new TrainingDriver(), otherArgs));
    }
}