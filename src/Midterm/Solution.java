package Midterm;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

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
    public static int cluster_num = 500;

    public static void main(String[] args){
        System.load(new File("/usr/local/Cellar/opencv/2.4.12/share/OpenCV/java/libopencv_java2412.dylib").getAbsolutePath());
        Solution s = new Solution();
        try {
            //s.produceLabeledDataForTrainingAndTesting(args[0], args[1], args[2]);
            //String[] trainArgs = {"files/FV"};
            s.produceSIFTFromImageData(args[0], args[1], args[2]);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void produceSIFTFromImageData(String dir, String output, String test) throws IOException {
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        List<Image> images = new ArrayList<Image>();
        Mat decriptors = new Mat();
        for (int i = 0; i < listOfFiles.length; i++){
            if (listOfFiles[i].isDirectory()){
                File[] iamge = listOfFiles[i].listFiles();

                for(File f : iamge) {
                    if (f.getName().endsWith("jpg")) {
                        Mat res = produceSIFTFeature(f.getAbsolutePath());
                        if (res == null || res.rows() == 0) {
                            continue;
                        }
                        Image img = new Image();
                        img.fileName = f.getAbsolutePath();
                        img.desc = res;
                        img.label = i;
                        images.add(img);
                        decriptors.push_back(res);
                    }
                }
            }
        }

        Mat labels = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
        Mat centers = new Mat();
        Core.kmeans(decriptors, Solution.cluster_num, labels, criteria, 100, Core.KMEANS_PP_CENTERS, centers);

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
            writeSIFTFeatureVector(img, labels, cur_row, cur_writer, Solution.cluster_num);

            cur_row += img.desc.rows();
            index++;
        }
        System.out.println("Finish");
    }

    private void writeSIFTFeatureVector(Image image, Mat label, int cur_row, BufferedWriter cur_writer, int N){
        int[] stat = new int[N];
        System.out.println(image.desc.rows());
        for(int i = cur_row; i < cur_row + image.desc.rows(); i++){
            int l = (int)label.get(i, 0)[0];
            stat[l]++;
        }

        double sum = 0;
        for(int j = 0; j < stat.length; j++){
            sum += stat[j];
        }

        try {
            cur_writer.write(image.label + "");
            for (int j = 0; j < stat.length; j++){
                if (stat[j] == 0){
                    continue;
                }

                cur_writer.write(" " + (j+1) + ":" + stat[j] / sum);
            }
            cur_writer.newLine();
            cur_writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Mat produceSIFTFeature(String imageName) throws IOException{
        Mat test_mat = Highgui.imread(imageName, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
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
