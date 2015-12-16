package Final;

/**
 * Representation of Document-Topic distribution.
 * @author amaliujia
 */
public class FileTopicDistribution {
    private int fileID;
    private String filename;
    private double[] distributions;
    private int cur_index = 0;

    public FileTopicDistribution(int topic_num){
        this.distributions = new double[topic_num];
    }

    public FileTopicDistribution(int fileID, String filename, int topic_num){
        this.fileID = fileID;
        this.filename = filename;
        this.distributions = new double[topic_num];
    }

    public int getFileID() {
        return this.fileID;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setDistributions(int index, double value) throws IndexOutOfBoundsException{
        if (index >= this.distributions.length) {
            throw new IndexOutOfBoundsException("index: " + index +
                    " is greater than length of array " + this.distributions.length);
        }

        this.distributions[index] = value;
    }

    public void addToDistribution(double value) {
        if (this.cur_index == this.distributions.length) {
            return;
        }

        this.distributions[this.cur_index++] = value;
    }
    public double getLikelihood(int id) {
        return this.distributions[id];
    }

}
