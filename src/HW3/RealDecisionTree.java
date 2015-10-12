import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by amaliujia on 15-10-11.
 */
public class RealDecisionTree {
    // the number of data records
    private int N;

    // number of data records used for testing.
    private int testN;

    // for testN, the number that were correctly identified
    private int correct;

    private TreeNode root;
    
    // This is a pointer to the Random Forest this decision tree belongs to
    private RandomForest forest;

    // This keeps track of all the predictions done by this tree
    public ArrayList<Integer> predictions;

    /**
     * This constructs a decision tree from a data matrix.
     */
    public RealDecisionTree(ArrayList<int[]> data,RandomForest forest,int num){
        this.forest=forest;
        N=data.size();
        //importances=new int[RandomForest.numAttr];
        predictions = new ArrayList<Integer>();

        // traing data set
        ArrayList<int[]> train=new ArrayList<int[]>(N);
        //test data set
        ArrayList<int[]> test=new ArrayList<int[]>();

        SplitSet(data, train, test, num);
        testN=test.size();
        correct=0;

        root=buildTree(train, num);
    }
    
    /**
     * This creates the decision tree according to the specifications of random forest trees.
     */
    private TreeNode buildTree(ArrayList<int[]> train, int ntree){
        TreeNode root=new TreeNode();
        root.data=train;
        RecursiveSplit(root,ntree);
        return root;
    }

    private class TreeNode{
        public boolean isLeaf;
        public TreeNode left;
        public TreeNode right;
        public int splitAttributeM;
        public Integer label;
        public List<int[]> data;
        public int splitValue;
        public int generation;

        public TreeNode(){
            splitAttributeM=-99;
            splitValue=-99;
            generation=1;
        }
        public TreeNode clone(){
            TreeNode copy=new TreeNode();
            copy.isLeaf=isLeaf;
            if (left != null)
                copy.left=left.clone();
            if (right != null)
                copy.right=right.clone();
            copy.splitAttributeM=splitAttributeM;
            copy.label=label;
            copy.splitValue=splitValue;
            return copy;
        }
    }
    private class DoubleWrapper{
        public double d;
        public DoubleWrapper(double d){
            this.d=d;
        }
    }
    /**
     * Keep splitting nodes until termination condition achieve.
     */
    private void RecursiveSplit(TreeNode parent, int Ntreenum){
        if (!parent.isLeaf){
            Integer Class=CheckIfLeaf(parent.data);
            if (Class != null){
                parent.isLeaf=true;
                parent.label=Class;
                return;
            }

            // not leaf, should split as a binary tree.
            int subNode=parent.data.size();
            parent.left=new TreeNode();
            parent.left.generation=parent.generation+1;
            parent.right=new TreeNode();
            parent.right.generation=parent.generation+1;
            ArrayList<Integer> vars=GetPartialRecords();

            DoubleWrapper doubleWrapper= new DoubleWrapper(Double.MAX_VALUE);

            for (int m:vars){
                SortAtAttribute(parent.data, m);//sorts on a particular column/attribute in the rows
                ArrayList<Integer> indicesToCheck=new ArrayList<Integer>();
                for (int n=1;n<subNode;n++){
                    int classA=getLabel(parent.data.get(n - 1));
                    int classB=getLabel(parent.data.get(n));
                    if (classA != classB)
                        indicesToCheck.add(n);
                }

                if (indicesToCheck.size() == 0){// if leaf, label leaf and continue.
                    parent.isLeaf=true;
                    parent.label=getLabel(parent.data.get(0));
                    continue;
                } else {
                    for (int n:indicesToCheck){
                        entropyAtCurrPosition(m, n, subNode, doubleWrapper, parent, Ntreenum);
                        if (doubleWrapper.d == 0)
                            break;
                    }
                }
            }

            // left child
            if (parent.left.data.size() == 1){ // no left data exist, should be leaf.
                parent.left.isLeaf=true;
                parent.left.label=getLabel(parent.left.data.get(0));
            } else if(CheckIfLeaf(parent.left.data) == null) {
                parent.left.isLeaf = false;
                parent.left.label = null;
            } else {
                    parent.left.isLeaf=true;
                    parent.left.label=Class;
            }

            // right child
            if (parent.right.data.size() == 1){
                parent.right.isLeaf=true;
                parent.right.label=getLabel(parent.right.data.get(0));
            } else if(CheckIfLeaf(parent.right.data) == null){
                    parent.right.isLeaf=false;
                    parent.right.label=null;
            } else {
                    parent.right.isLeaf=true;
                    parent.right.label=Class;
            }

            // if left or right node is not leaf yet, keep splitting.
            if (!parent.left.isLeaf)
                RecursiveSplit(parent.left,Ntreenum);
            if (!parent.right.isLeaf)
                RecursiveSplit(parent.right,Ntreenum);
        }
    }

    /**
     * The total entropy is calculated by getting the sub-entropy for below the split point and after the split point.
     * The sub-entropy is calculated by first getting the proportion of each of the label in this sub-data matrix.
     */
    private double entropyAtCurrPosition(int m,int n,int Nsub, DoubleWrapper lowestE,TreeNode parent, int nTre){
        if (n < 1)
            return 0;
        if (n > Nsub)
            return 0;

        List<int[]> lower = getLower(parent.data,n);
        List<int[]> upper = getUpper(parent.data,n);
        double[] pl=getLabelProbs(lower);
        double[] pu=getLabelProbs(upper);
        double eL=crossEntropy(pl);
        double eU=crossEntropy(pu);

        double e=(eL*lower.size()+eU*upper.size())/((double)Nsub);

        if (e < lowestE.d){
            lowestE.d=e;
            parent.splitAttributeM=m;
            parent.splitValue=parent.data.get(n)[m];
            parent.left.data=lower;
            parent.right.data=upper;
        }
        return e;
    }


    // For a given instance, how to label it.
    public int predict(int[] record){
        TreeNode node = root;

        while (true){
            if (node.isLeaf)
                return node.label;
            if (record[node.splitAttributeM] <= node.splitValue)
                node=node.left;
            else
                node=node.right;
        }
    }

    /**
     * Given a data record, return the label.
     */
    public static int getLabel(int[] record) {
        return record[RandomForest.numAttr];
    }
    
    /**
     * Given a data matrix, check if all the label are the same. If so,
     * return that label, null if not
     */
    private Integer CheckIfLeaf(List<int[]> data){
        boolean isLeaf=true;
        int ClassA=getLabel(data.get(0));
        for (int i=1;i<data.size();i++){
            int[] recordB=data.get(i);
            if (ClassA != getLabel(recordB)){
                isLeaf=false;
                break;
            }
        }
        if (isLeaf)
            return getLabel(data.get(0));
        else
            return null;
    }
    /**
     * Split a data matrix and return the upper portion
     */
    private List<int[]> getUpper(List<int[]> data, int nSplit){
        int N=data.size();
        List<int[]> upper=new ArrayList<int[]>(N-nSplit);
        for (int n=nSplit;n<N;n++)
            upper.add(data.get(n));
        return upper;
    }
    
    /**
     * Split a data matrix and return the lower portion
     */
    private List<int[]> getLower(List<int[]> data,int nSplit){
        List<int[]> lower=new ArrayList<int[]>(nSplit);
        for (int n=0;n<nSplit;n++)
            lower.add(data.get(n));
        return lower;
    }
    /**
     * Sort records based on one contribute. After sort I can get lower and upper of matrix
     * separately.
     */
    private class AttributeComparator implements Comparator{
        private int m;
        public AttributeComparator(int m){
            this.m=m;
        }
        public int compare(Object o1, Object o2){
            int a=((int[])o1)[m];
            int b=((int[])o2)[m];
            if (a < b)
                return -1;
            if (a > b)
                return 1;
            else
                return 0;
        }
    }
    /**
     * Sorts a data matrix by an attribute from lowest record to highest record
     */
    private void SortAtAttribute(List<int[]> data,int m){
        Collections.sort(data, (Comparator<? super int[]>) new AttributeComparator(m));
    }
    /**
     * Given a data matrix, return a probabilty mass function representing
     * the frequencies of a label (aka 1 or 0)
     */
    private double[] getLabelProbs(List<int[]> records){

        double N=records.size();

        int[] counts=new int[RandomForest.numLabels];
        for (int[] record:records)
            counts[getLabel(record)-1]++;

        double[] ps=new double[RandomForest.numLabels];
        for (int c=0;c<RandomForest.numLabels;c++)
            ps[c]=counts[c]/N;
        return ps;
    }

    /**
     * Compute the cross entropy.
     */
    private double crossEntropy(double[] ps){
        double e=0;
        for (double p:ps){
            if (p != 0)
                e += p*Math.log(p)/Math.log(2);
        }
        return -e;
    }

    /**
     * Randomly select attributes for training.
     * @return
     *      The records that chosen to split node.
     */
    private ArrayList<Integer> GetPartialRecords() {
        boolean[] vars=new boolean[RandomForest.numAttr];

        for (int i=0;i<RandomForest.numAttr;i++)
            vars[i]=false;

        while (true){
            int t=(int)Math.floor(Math.random() * RandomForest.numAttr);
            vars[t]=true;
            int N=0;
            for (int i=0;i<RandomForest.numAttr;i++)
                if (vars[i])
                    N++;
            if (N == RandomForest.numAttr)
                break;
        }

        // Get numAttrRandom records.
        ArrayList<Integer> records=new ArrayList<Integer>(RandomForest.numAttrRandom);

        for (int i=0;i<RandomForest.numAttrRandom;i++)
            if (vars[i])
                records.add(i);
        return records;
    }

    /**
     * Split the data set to train set and test data.
     */
    private void SplitSet(ArrayList<int[]> data,ArrayList<int[]> train,ArrayList<int[]> test,int numb){
        ArrayList<Integer> indices=new ArrayList<Integer>(N);
        // crate random N numbers.
        for (int n=0;n<N;n++)
            indices.add((int)Math.floor(Math.random() * N));

        ArrayList<Boolean> in=new ArrayList<Boolean>(N);
        for (int n=0;n<N;n++)
            in.add(false);
        for (int num:indices){
            train.add((data.get(num)).clone());
            in.set(num,true);
        }
        for (int i=0;i<N;i++)
            if (!in.get(i))
                test.add((data.get(i)).clone());
    }

}