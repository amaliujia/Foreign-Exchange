package Midterm;

import org.opencv.core.Mat;

/**
 * Created by amaliujia on 15-10-18.
 */
public class Image {
    public String fileName;
    // SIFT feature
    public Mat desc;
    //HOG feature
    public float[] hog;
    //Image labels
    public int label;
}
