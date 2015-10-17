package Midterm;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by amaliujia on 15-10-15.
 */
public class Solution {
    public static void main(String[] args){
        Solution s = new Solution();
        try {
            //s.produceLabeledData(args[0], args[1]);
            String[] trainArgs = {"files/FV"};
            String model_file = svm_train.main(trainArgs);

        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    writer.write(i);
                    for(int j = 0; j < pixels.length; j++){
                            writer.write(" " + (j + 1) + ":");
                            String s = "" + pixels[j];
                            writer.write(s);
                    }
                    writer.newLine();
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
//                    int rgb[] = new int[] {
//                            (argb >> 16) & 0xff, //red
//                            (argb >>  8) & 0xff, //green
//                            (argb      ) & 0xff  //blue
//                    };
//                    res[index++] = rgb[0];
//                    res[index++] = rgb[1];
//                    res[index++] = rgb[2];
                    res[index++] = argb;
                }catch (ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                }

            }
        }
       // int[] dataRes = bufferedImage.getRaster().getDataBuffer();
        return res;
    }
}
