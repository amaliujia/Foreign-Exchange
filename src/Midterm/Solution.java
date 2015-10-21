package Midterm;

import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by amaliujia on 15-10-15.
 */
public class Solution {
    public static int cluster_num = 20;
    public static boolean HOGFeature = true;
    public static boolean SIFTFeature = true;

    public static void main(String[] args){
        System.load(new File("/usr/local/Cellar/opencv/2.4.12/share/OpenCV/java/libopencv_java2412.dylib").getAbsolutePath());
        Solution s = new Solution();
        try {
            //s.produceLabeledFullDataForTrainingAndTesting(args[0], args[1], args[2]);
            //String[] trainArgs = {"files/FV"};
            s.produceFeatureFromImageData(args[0], args[1], args[2]);
            //s.produceHOGFeature("/Users/amaliujia/Documents/CMU/Fall2015/11676/midterm/train2/acantharia_protist/100224.jpg");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function process all the images and extract HOG and SIFT feature. 20% images are used for test, others are
     * for training.
     * @param dir
     * @param output
     * @param test
     * @throws IOException
     */
    private void produceFeatureFromImageData(String dir, String output, String test) throws IOException {
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        List<Image> images = new ArrayList<Image>();
        Mat decriptors = new Mat();
        for (int i = 0; i < listOfFiles.length; i++){
            if (listOfFiles[i].isDirectory()){
                File[] iamge = listOfFiles[i].listFiles();

                for(File f : iamge) {
                    if (f.getName().endsWith("jpg")) {
                        Image img = new Image();
                        img.fileName = f.getAbsolutePath();
                        img.label = i;

                        if (Solution.SIFTFeature){
                            Mat res = produceSIFTFeature(f.getAbsolutePath());
                            if (res == null || res.rows() == 0) {
                                continue;
                            }
                            img.desc = res;
                            decriptors.push_back(res);
                        }


                        if (HOGFeature){
                            img.hog = produceHOGFeature(f.getAbsolutePath());
                        }
                        images.add(img);
                    }
                }
            }
        }

        Mat labels = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
        Mat centers = new Mat();
        if (Solution.SIFTFeature)
            Core.kmeans(decriptors, Solution.cluster_num, labels, criteria, 50, Core.KMEANS_PP_CENTERS, centers);

        System.out.println("Clustering down....");
        System.out.println(decriptors.rows() + "  " + labels.rows() + "  " + labels.cols());

        BufferedWriter train_writer = new BufferedWriter(new FileWriter(new File(output)));
        BufferedWriter test_writer = new BufferedWriter(new FileWriter(new File(test)));

        int cur_row = 0;
        int index = 0;
        BufferedWriter cur_writer = null;
        for (Image img : images){

            if (index % 5 == 0){
                cur_writer = test_writer;
            }else{
                cur_writer = train_writer;
            }
            writeFeatureVector(img, labels, cur_row, cur_writer, Solution.cluster_num);

            cur_writer.newLine();
            cur_writer.flush();
            if (Solution.SIFTFeature)
                cur_row += img.desc.rows();

            index++;
        }
        System.out.println("Finish");
    }

    /**
     * Generate feature vector of HOG and SIFT, write to file.
     * @param image
     * @param label
     * @param cur_row
     * @param cur_writer
     * @param N
     */
    private void writeFeatureVector(Image image, Mat label, int cur_row, BufferedWriter cur_writer, int N){
        int[] stat = new int[N];
        double sum = 0;
        if (Solution.SIFTFeature){
            //System.out.println(image.desc.rows());
            for(int i = cur_row; i < cur_row + image.desc.rows(); i++){
                int l = (int)label.get(i, 0)[0];
                stat[l]++;
            }

            for(int j = 0; j < stat.length; j++){
                sum += stat[j];
            }
        }

        try {
            cur_writer.write(image.label + "");
            int j = 0;
            if (Solution.SIFTFeature){
                for (; j < stat.length; j++){
                    if (stat[j] == 0){
                        continue;
                    }
                    cur_writer.write(" " + (j+1) + ":" + stat[j] / sum);
                }
            }

            if (Solution.HOGFeature){
                for (int x = 0; x < image.hog.length; j++, x++){
                    if (image.hog[x] == 0){
                        continue;
                    }
                    cur_writer.write(" " + (j+1) + ":" + image.hog[x]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Extract HOG feature from image.
     * @param imageName
     * @return
     * @throws IOException
     */
    private float[] produceHOGFeature(String imageName) throws IOException{
        Mat test_mat = Highgui.imread(imageName, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat target = new Mat();
        Size size = new Size(40,24);
        Imgproc.resize(test_mat, target, size);
        test_mat = target;
        MatOfFloat descriptors = new MatOfFloat();
        Mat desc = new Mat();
        Size winSize = new Size(40, 24);
        Size blockSize = new Size(8, 8);
        Size blockStride = new Size(16, 16);
        Size cellSize = new Size(8, 8);
        int nBins = 9;
        Size winStride = new Size(16, 16);
        Size padding = new Size(0, 0);
        HOGDescriptor hogDescriptor = new HOGDescriptor(winSize, blockSize, blockStride, cellSize, nBins);
        hogDescriptor.compute(test_mat, descriptors);
        //System.out.println(descriptors.rows() + " " + descriptors.cols());
        return descriptors.toArray();
    }

    /**
     * Extract SIFT feature from image
     * @param imageName
     * @return
     *      Mat in opencv
     * @throws IOException
     */
    private Mat produceSIFTFeature(String imageName) throws IOException{
        Mat test_mat = Highgui.imread(imageName, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        //System.out.println(test_mat.rows() + " " + test_mat.cols());
        Mat desc = new Mat();
        FeatureDetector fd = FeatureDetector.create(FeatureDetector.SIFT);
        MatOfKeyPoint mkp =new MatOfKeyPoint();
        fd.detect(test_mat, mkp);
        DescriptorExtractor de = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        de.compute(test_mat,mkp,desc);
        return desc;
    }


    private void produceLabeledData(String dir, String output) throws IOException {
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
        for (int i = 0; i < listOfFiles.length; i++){
            if (listOfFiles[i].isDirectory()){
                File[] iamge = listOfFiles[i].listFiles();

                for(File f : iamge){
                    int[] pixels = processImage(f);
                    if (pixels == null) continue;
                    int index = 1;
                    writer.write(i + "");
                    for(int j = 0; j < pixels.length; j += 50){
                        int mx = Integer.MIN_VALUE;
                        int mn = Integer.MAX_VALUE;
                        int total = 0;
                        for(int z = j; z < pixels.length && z < j + 50; z++){
                            mx = Math.max(mx, pixels[z]);
                            mn = Math.min(mn, pixels[z]);
                            total += pixels[z];
                        }
                        writer.write(" " + index++ + ":" + mx);
                        writer.write(" " + index++ + ":" + mn);
                        writer.write(" " + index++ + ":" + total * 1.0 / 50);
                    }
                    writer.newLine();
                }
            }
        }
    }

    /**
     *
     * @param dir
     * @param output
     * @throws IOException
     */
    private void produceTestData(String dir, String output) throws IOException {
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
        for (int i = 0; i < listOfFiles.length; i++){
            if (listOfFiles[i].isDirectory()){
                File[] iamge = listOfFiles[i].listFiles();

                for(File f : iamge){
                    int[] pixels = processImage(f);
                    if (pixels == null) continue;
                    //writer.write(i + "");
                    for(int j = 0; j < pixels.length; j++){
                        writer.write((j + 1) + ":");
                        String s = pixels[j] + " ";
                        writer.write(s);
                    }
                    writer.newLine();
                }
            }
        }

    }

    /**
     * Deal with a directory which contains lots of image. Divide these images to two cluster, one for training and
     * another for testing. Output every pixel.
     * @param dir
     *          The path to all images
     * @param output
     *          The path for training data.
     * @param Test
     *          The past for testing data.
     * @throws IOException
     */
    private void produceLabeledFullDataForTrainingAndTesting(String dir, String output, String Test) throws IOException {
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        BufferedWriter train_writer = new BufferedWriter(new FileWriter(new File(output)));
        BufferedWriter test_writer = new BufferedWriter(new FileWriter(new File(Test)));
        for (int i = 0; i < listOfFiles.length; i++){
            if (listOfFiles[i].isDirectory()){
                File[] iamge = listOfFiles[i].listFiles();

                for(int x = 0; x < iamge.length; x++) {
                    File f = iamge[x];
                    int[] pixels = processImage(f);
                    if (pixels == null) continue;
                    int index = 1;
                    BufferedWriter writer = null;
                    if (x % 5 == 0){
                        writer = test_writer;
                    }else{
                        writer = train_writer;
                    }
                    writer.write(i + "");
                    for(int j = 0; j < pixels.length; j++){
                        writer.write(" " + j + ":" + pixels[j]);
                    }
                    writer.newLine();
                    writer.flush();
                }
            }
        }
    }

    /**
     * Deal with a directory which contains lots of image. Divide these images to two cluster, one for training and
     * another for testing. Compute the 3 key important values for every 50 pixels.
     * @param dir
     *          The path to all images
     * @param output
     *          The path for training data.
     * @param Test
     *          The past for testing data.
     * @throws IOException
     */
    private void produceLabeledDataForTrainingAndTesting(String dir, String output, String Test) throws IOException {
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        BufferedWriter train_writer = new BufferedWriter(new FileWriter(new File(output)));
        BufferedWriter test_writer = new BufferedWriter(new FileWriter(new File(Test)));
        for (int i = 0; i < listOfFiles.length; i++){
            if (listOfFiles[i].isDirectory()){
                File[] iamge = listOfFiles[i].listFiles();

                for(int x = 0; x < iamge.length; x++) {
                    File f = iamge[x];
                    int[] pixels = processImage(f);
                    if (pixels == null) continue;
                    int index = 1;
                    BufferedWriter writer = null;
                    if (x % 5 == 0){
                        writer = test_writer;
                    }else{
                        writer = train_writer;
                    }
                    writer.write(i + "");
                    int mx = Integer.MIN_VALUE;
                    int mn = Integer.MAX_VALUE;
                    int total = 0;
                    for(int j = 0; j < pixels.length; j += 50){
                        for(int z = j; z < pixels.length && z < j + 50; z++){
                            mx = Math.max(mx, pixels[z]);
                            mn = Math.min(mn, pixels[z]);
                            total += pixels[z];
                        }
                        writer.write(" " + index++ + ":" + mx);
                        writer.write(" " + index++ + ":" + mn);
                        writer.write(" " + index++ + ":" + total * 1.0 / 50);
                    }
                    writer.newLine();
                    writer.flush();
                }
            }
        }
    }

    /**
     * Given each image, return an array that contains all the pixels.
     * @param imageName
     *          Filename of image
     * @return
     *          Array of pixels
     * @throws IOException
     */
    private int[] processImage(File imageName) throws IOException {
        // open image
        //File imgPath = new File(imageName);
        BufferedImage bufferedImage = ImageIO.read(imageName);
        if (bufferedImage == null) {
            return null;
        }
        int[] res = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
        int index = 0;
        for (int i = 0; i < bufferedImage.getHeight(); i++){
            for (int j = 0; j < bufferedImage.getWidth(); j++){
                try {
                    int argb = bufferedImage.getRGB(j, i);
                    res[index++] = argb;
                }catch (ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                }

            }
        }
        return res;
    }
}
