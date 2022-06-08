import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeSet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;

 
public class Demo extends Component implements ActionListener {

    // Contains the images loaded
    private ArrayList<BufferedImage> arrayImages = new ArrayList<BufferedImage>();
     // Contains the processed images
    private ArrayList<BufferedImage> arrayImagesFiltered = new ArrayList<BufferedImage>();


    private ArrayList<Integer> topWidth = new ArrayList<Integer>();
    private ArrayList<Integer> bottomWidth = new ArrayList<Integer>();

    private int image1;
    private int image2;

    // Size of graphic obejct to draw on screen
    private int windowWidth;
    private int windowHeight;
    
    //************************************
    // List of the options(Original, Negative); correspond to the cases:
    //************************************
  
    String descs[] = {"Original", "Negative", "Rescale & Shift Image (WITHOUT min and max)", "Rescale & Shift Image (WITH min and max)", "Addition", "Subtraction",
                        "Multiplication", "Division", "Bitwise NOT", "Bitwise AND", "Bitwise OR", "Bitwise XOR", "Logarithmic", "Power-Law", "Random Look-Up Table (LUT)",
                        "Bit-Plane Slicing", "Histogram Normalisation", "Histogram Equalisation", "Convolution Average", "Convolution Weighted Average", 
                        "4-neighbour Laplacian", "8-neighbour Laplacian", "4-neighbour Laplacian Enhancement", "8-neighbour Laplacian Enhancement", "Roberts 1",
                        "Roberts 2", "Sobel X", "Sobel Y", "Salt & Pepper Noise", "Min Filtering", "Max Filtering", "Mid-Point Filtering", "Median Filtering", 
                        "Simple Thresholding"};
 
    int opIndex;  //option index for 
    int lastOp;

    //int currIndex;
    //int prevIndex;

    private BufferedImage bi, biFiltered;   // the input image saved as bi;//
    int w, h;
     
    public Demo() {
        windowWidth = 1000;
        windowHeight = 1000;

    
        
    }                         
 
    public Dimension getPreferredSize() {
        return new Dimension(windowWidth, windowHeight);
    }
 

    String[] getDescriptions() {
        return descs;
    }

    // Return the formats sorted alphabetically and in lower case
    public String[] getFormats() {
        String[] formats = {"bmp","gif","jpeg","jpg","png"};
        TreeSet<String> formatSet = new TreeSet<String>();
        for (String s : formats) {
            formatSet.add(s.toLowerCase());
        }
        return formatSet.toArray(new String[0]);
    }
 
 

    void setOpIndex(int i) {
        opIndex = i;
    }
 
    public void paint(Graphics g) { //  Repaint will call this function so the image will change.
        filterImage();    
        //g.drawImage(biFiltered, 0, 0, null);

        int widthTop = 0;
        int widthBottom = 0;
        int height = 0;
        int sizeArrayImages = arrayImages.size();
        int sizeArrayImagesFiltered = arrayImagesFiltered.size();

        for(int i=0; i<sizeArrayImages; i++){
            // Print the original images on the same row
            if (i <  sizeArrayImages){
                topWidth.add(arrayImages.get(i).getWidth());
                g.drawImage(arrayImages.get(i), widthTop, 0, null);
                widthTop += topWidth.get(i);
                height = arrayImages.get(i).getHeight();
            }
            
            else{
                return;
            }
        }

        for(int i=0; i<sizeArrayImagesFiltered; i++){  
            // Print processes images below the original images
            if (i <  sizeArrayImagesFiltered){ 
                bottomWidth.add(arrayImagesFiltered.get(i).getWidth());
                g.drawImage(arrayImagesFiltered.get(i), widthBottom, height, null);
                widthBottom += bottomWidth.get(i);
            }
            
            else{  
                return;
            }
        }
    
                
        return; 
    }
 

    //************************************
    //  Convert the Buffered Image to Array
    //************************************
    private static int[][][] convertToArray(BufferedImage image){
      int width = image.getWidth();
      int height = image.getHeight();

      int[][][] result = new int[width][height][4];

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int p = image.getRGB(x,y);
            int a = (p>>24)&0xff;
            int r = (p>>16)&0xff;
            int g = (p>>8)&0xff;
            int b = p&0xff;

            result[x][y][0]=a;
            result[x][y][1]=r;
            result[x][y][2]=g;
            result[x][y][3]=b;
         }
      }
      return result;
    }

    //************************************
    //  Convert the  Array to BufferedImage
    //************************************
    public BufferedImage convertToBimage(int[][][] TmpArray){

        int width = TmpArray.length;
        int height = TmpArray[0].length;

        BufferedImage tmpimg=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                int a = TmpArray[x][y][0];
                int r = TmpArray[x][y][1];
                int g = TmpArray[x][y][2];
                int b = TmpArray[x][y][3];
                
                //set RGB value

                int p = (a<<24) | (r<<16) | (g<<8) | b;
                tmpimg.setRGB(x, y, p);

            }
        }
        return tmpimg;
    }



    // Populate the arrays with images 
    public void setImages(String img){
        try{
            
            arrayImages.add(ImageIO.read(new File(img)));

        }
        // deal with the situation that th image has problem/
        catch(IOException e) { 
            System.out.println("Image could not be read");
            //System.exit(1);
        }
        repaint();
        return;
    }


   






    //************************************
    //  Example:  Image Negative
    //************************************
    public BufferedImage ImageNegative(BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] imageArray1 = convertToArray(timg);          //  Convert the image to array

        // Image Negative Operation:
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                imageArray1[x][y][1] = 255-imageArray1[x][y][1];  //r
                imageArray1[x][y][2] = 255-imageArray1[x][y][2];  //g
                imageArray1[x][y][3] = 255-imageArray1[x][y][3];  //b
            }
        }
        
        return convertToBimage(imageArray1);  // Convert the array to BufferedImage
    }



    //************************************
    //  RESCALING 
    //************************************
    //the rescale input that takes rescale and shift factor from user
    public BufferedImage rescaleInput(BufferedImage timg, int opt) {
        JFrame f = new JFrame();
        String inputString1 = JOptionPane.showInputDialog(f, "Input the rescale factor (0 - 2)");
        String inputString2 = JOptionPane.showInputDialog(f, "Input the shift factor");

        Float s = Float.valueOf(inputString1).floatValue();
        if (s > 2) {
            s = (float) 2.0;
        } else if (s < 0) {
            s = (float) 0.0;
        }
        int t = Integer.valueOf(inputString2);

        BufferedImage outputImage = bi; 
        if (opt == 0){
             outputImage = rescaleImage(timg, s, t);
            
        }

        if (opt == 1){
             outputImage = shiftRescale(timg, s, t);
        }

       
        return outputImage;
            
    }




    // To shift by t and rescale by s without finding the min and the max
    public BufferedImage rescaleImage(BufferedImage timg, Float s, int t) {
        // Normal Values are 1 & 255

        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] imageArray1 = convertToArray(timg); // Convert the image to array
        int[][][] imageArray2 = imageArray1;
        // To shift by t and rescale by s without finding the min and the max
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                imageArray2[x][y][1] = (int) (s * (imageArray1[x][y][1] + t)); // r
                imageArray2[x][y][2] = (int) (s * (imageArray1[x][y][2] + t)); // g
                imageArray2[x][y][3] = (int) (s * (imageArray1[x][y][3] + t)); // b
                if (imageArray2[x][y][1] < 0) {
                    imageArray2[x][y][1] = 0;
                }
                if (imageArray2[x][y][2] < 0) {
                    imageArray2[x][y][2] = 0;
                }
                if (imageArray2[x][y][3] < 0) {
                    imageArray2[x][y][3] = 0;
                }
                if (imageArray2[x][y][1] > 255) {
                    imageArray2[x][y][1] = 255;
                }
                if (imageArray2[x][y][2] > 255) {
                    imageArray2[x][y][2] = 255;
                }
                if (imageArray2[x][y][3] > 255) {
                    imageArray2[x][y][3] = 255;
                }
            }
        }
        return convertToBimage(imageArray2); // Convert the array to BufferedImage
    }



    // To shift by t and rescale by s and find the min and the max
    public BufferedImage shiftRescale(BufferedImage timg, float scalingFactor, int shifting){
        int rmin, rmax, gmin, gmax, bmin, bmax;
        double s = scalingFactor;
        int t = shifting;
        int width = timg.getWidth();
        int height = timg.getHeight();
        int [][][] imageArray1 = convertToArray(timg);
        int [][][] imageArray2 = new int[height][width][4];

        rmin = (int)Math.round(s*(imageArray1[0][0][1]+t)); rmax = rmin;
        gmin = (int)Math.round(s*(imageArray1[0][0][2]+t)); gmax = gmin;
        bmin =(int) Math.round(s*(imageArray1[0][0][3]+t)); bmax = bmin;
        for(int y=0; y<height; y++){
            for(int x=0; x<width; x++){
                imageArray2[x][y][1] = (int)Math.round(s*(imageArray1[x][y][1]+t)); //r
                imageArray2[x][y][2] = (int)Math.round(s*(imageArray1[x][y][2]+t)); //g
                imageArray2[x][y][3] = (int)Math.round(s*(imageArray1[x][y][3]+t)); //b
                if (rmin>imageArray2[x][y][1]) { rmin = imageArray2[x][y][1]; }
                if (gmin>imageArray2[x][y][2]) { gmin = imageArray2[x][y][2]; }
                if (bmin>imageArray2[x][y][3]) { bmin = imageArray2[x][y][3]; }
                if (rmax<imageArray2[x][y][1]) { rmax = imageArray2[x][y][1]; }
                if (gmax<imageArray2[x][y][2]) { gmax = imageArray2[x][y][2]; }
                if (bmax<imageArray2[x][y][3]) { bmax = imageArray2[x][y][3]; }
            }
        }

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                imageArray2[x][y][1]=255*(imageArray2[x][y][1]-rmin)/(rmax-rmin);
                imageArray2[x][y][2]=255*(imageArray2[x][y][2]-gmin)/(gmax-gmin);
                imageArray2[x][y][3]=255*(imageArray2[x][y][3]-bmin)/(bmax-bmin);
            }
        }
        return convertToBimage(imageArray2);
    }


    //************************************
    //  ARITHMETIC & BOOLEAN
    //************************************
    //to select which 2 images to process
    public void imageNumberProcess() {
        JFrame f = new JFrame();

        if (arrayImages.size()>=2){
            String inputString1 = JOptionPane.showInputDialog(f, "Enter image 1: ");
            String inputString2 = JOptionPane.showInputDialog(f, "Enter image 2: ");

            image1 = Integer.parseInt(inputString1)-1;
            image2 = Integer.parseInt(inputString2)-1;
        }

        else{
            //JFrame f = new JFrame();
            JOptionPane.showMessageDialog(f, "Please select another image before doing this function"); 
        }

        return;
    }


    // Arithmetic operations for image enhancement
    // ADDITION
    public BufferedImage addtion(BufferedImage timg, BufferedImage timg2) {
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg2);
        int[][][] imageArray3 = new int[imageArray1.length][imageArray1[0].length][4]; // Creates Array to store final output of addition operation

        for (int y = 0; y < timg.getHeight(); y++) {
            for (int x = 0; x < timg.getWidth(); x++) {
                imageArray3[x][y][1] = (imageArray1[x][y][1] + imageArray2[x][y][1]);
                imageArray3[x][y][2] = (imageArray1[x][y][2] + imageArray2[x][y][2]);
                imageArray3[x][y][3] = (imageArray1[x][y][3] + imageArray2[x][y][3]);
            }
        }

        return shiftRescale(convertToBimage(imageArray3), (float)255.0, 1);
    }


    // SUBTRACTION
    public BufferedImage subtraction(BufferedImage timg, BufferedImage timg2) {
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg2);
        int[][][] imageArray3 = new int[imageArray1.length][imageArray1[0].length][4]; // Creates Array to store final output of subtraction operation

        for (int y = 0; y < timg.getHeight(); y++) {
            for (int x = 0; x < timg.getWidth(); x++) {
                imageArray3[x][y][1] = (imageArray1[x][y][1] - imageArray2[x][y][1]);
                imageArray3[x][y][2] = (imageArray1[x][y][2] - imageArray2[x][y][2]);
                imageArray3[x][y][3] = (imageArray1[x][y][3] - imageArray2[x][y][3]);
            }
        }

        return shiftRescale(convertToBimage(imageArray3), (float)255.0, 1);
    }


    // MULTIPLICATION
    public BufferedImage multiplication(BufferedImage timg, BufferedImage timg2){
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg2);
        int[][][] imageArray3 = new int[imageArray1.length][imageArray1[0].length][4];

        for(int y=0; y < timg.getWidth(); y++){
            for(int x=0; x < timg.getHeight(); x++){
                imageArray3[x][y][1] = imageArray1[x][y][1] * imageArray2[x][y][1];
                imageArray3[x][y][2] = imageArray1[x][y][2] * imageArray2[x][y][2];
                imageArray3[x][y][3] = imageArray1[x][y][3] * imageArray2[x][y][3];
            }
        }

        return shiftRescale(convertToBimage(imageArray3), (float)255.0, 1);
    }


    // DIVISION
    public BufferedImage division(BufferedImage timg, BufferedImage timg2){
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg2);
        int[][][] imageArray3 = new int[imageArray1.length][imageArray1[0].length][4];
        
        for(int y=0; y < timg.getWidth(); y++){
            for(int x=0; x < timg.getHeight(); x++){
                if(imageArray2[x][y][1] == 0){
                    imageArray3[x][y][1] = imageArray1[x][y][1];
                }
                else{
                    imageArray3[x][y][1] = imageArray1[x][y][1] / imageArray2[x][y][1];
                }
                if(imageArray2[x][y][2] == 0){
                     imageArray3[x][y][2] = imageArray1[x][y][2];
                }
                else{
                    imageArray3[x][y][2] = imageArray1[x][y][2] / imageArray2[x][y][2];
                }
                if(imageArray2[x][y][3] == 0){
                     imageArray3[x][y][3] = imageArray1[x][y][3];
                }
                else{
                    imageArray3[x][y][3] = imageArray1[x][y][3] / imageArray2[x][y][3];
                }
            }
        }
            return shiftRescale(convertToBimage(imageArray3), (float)255.0, 1);
    }
    

    // NOT
    public BufferedImage bitwiseNOT(BufferedImage timg){
        int[][][] imageArray1 = convertToArray(timg);

        for(int y = 0; y < timg.getHeight(); y++){
            for(int x = 0; x < timg.getWidth(); x++){
                int r = imageArray1[x][y][1];
                int g = imageArray1[x][y][2];
                int b = imageArray1[x][y][3];
                imageArray1[x][y][1] = (~r)& 0xFF; 
                imageArray1[x][y][2] = (~g)& 0xFF; 
                imageArray1[x][y][3] = (~b)& 0xFF; 
            }
        }
        return convertToBimage(imageArray1);
    }


    // AND
    public BufferedImage bitwiseAND(BufferedImage timg, BufferedImage timg2){
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg2);

        for(int y = 0; y < timg.getHeight(); y++){
            for(int x = 0; x < timg.getWidth(); x++){
                imageArray1[x][y][1] = (imageArray1[x][y][1] & imageArray2[x][y][1])& 0xFF; 
                imageArray1[x][y][2] = (imageArray1[x][y][2] & imageArray2[x][y][2])& 0xFF;
                imageArray1[x][y][3] = (imageArray1[x][y][3] & imageArray2[x][y][3])& 0xFF;
            }
        }
        return convertToBimage(imageArray1);
    }


    // OR
    public BufferedImage bitwiseOR(BufferedImage timg, BufferedImage timg2){
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg2);

        for(int y = 0; y < timg.getHeight(); y++){
            for(int x = 0; x < timg.getWidth(); x++){
                imageArray1[x][y][1] = (imageArray1[x][y][1] | imageArray2[x][y][1])& 0xFF; 
                imageArray1[x][y][2] = (imageArray1[x][y][2] | imageArray2[x][y][2])& 0xFF;
                imageArray1[x][y][3] = (imageArray1[x][y][3] | imageArray2[x][y][3])& 0xFF;
            }
        }
        return convertToBimage(imageArray1);
    }


    // XOR
    public BufferedImage bitwiseXOR(BufferedImage timg, BufferedImage timg2){
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg2);

        for(int y = 0; y < timg.getHeight(); y++){
            for(int x = 0; x < timg.getWidth(); x++){
                imageArray1[x][y][1] = (imageArray1[x][y][1] ^ imageArray2[x][y][1])& 0xFF; 
                imageArray1[x][y][2] = (imageArray1[x][y][2] ^ imageArray2[x][y][2])& 0xFF;
                imageArray1[x][y][3] = (imageArray1[x][y][3] ^ imageArray2[x][y][3])& 0xFF;
            }
        }
        return convertToBimage(imageArray1);
    }



    //*******************************************
    //  POINT PROCESSING & BIT-PLANE SLICING
    //*******************************************

   // LOGARITHMIC FUNCTION
    public BufferedImage logarithmic(BufferedImage timg) {
        int [][][] imageArray1 = convertToArray(timg);

        //To apply logarithmic function s = c log(1+r) to images
        double c = 255/(Math.log(256));
        for(int y=0; y < timg.getHeight(); y++){
            for(int x=0; x < timg.getWidth(); x++){
                for(int p = 1; p < 4; p++){
                    imageArray1[x][y][p] = (int)(c * Math.log(imageArray1[x][y][p]));
                }
            }
        }

        return convertToBimage(imageArray1);
    }


    
    // POWER-LAW FUNCTION
    public BufferedImage powerLaw(BufferedImage timg) {
        JFrame f = new JFrame();
        String inputString1 = JOptionPane.showInputDialog(f, "Input the power value (0.01 - 0.25)");
        double p = Float.valueOf(inputString1).floatValue();
        if (p > 25) {
            p = (double) 25.0;
        } else if (p < 0.01) {
            p = (double) 0.01;
        }

        BufferedImage outputImage = powerFunction(timg, p);
        return outputImage;
    }

    public BufferedImage powerFunction(BufferedImage timg, double p) {
        int [][][] imageArray1 = convertToArray(timg);

        //To apply power law s = c r^p to images with different powers from 0.01 to 25
        double c = Math.pow(255, 1-p);
        
        //Image is 255, so we use Y = 2.5
        double Y = 2.5;

        for(int y=0; y < timg.getHeight(); y++){
            for(int x=0; x < timg.getWidth(); x++){
                for(int t = 1; t < 4; t++){
                    imageArray1[x][y][t] = (int)(c * Math.pow(imageArray1[x][y][t],Y)); 
                }
            }
        }

        return convertToBimage(imageArray1);
    }


    // RANDOM LOOK-UP TABLE
    public BufferedImage randomLookupTable(BufferedImage timg){
        int[][][] imageArray1 = convertToArray(timg);

        int[] lut = new int[256];
        Random rnd = new Random();

        for(int i = 0; i < lut.length; i++){
            lut[i] = rnd.nextInt(256);
        }

        for(int y = 0; y < timg.getHeight(); y++){
            for(int x = 0; x < timg.getWidth(); x++){
                imageArray1[x][y][1] = lut[imageArray1[x][y][1]]; 
                imageArray1[x][y][2] = lut[imageArray1[x][y][2]];
                imageArray1[x][y][3] = lut[imageArray1[x][y][3]];
            }
        }
        return convertToBimage(imageArray1);
    }


    // BIT-PLANE SLICING
    public BufferedImage bitPlaneSlicing(BufferedImage timg){
        int[][][] imageArray1 = convertToArray(timg);

        JFrame f = new JFrame();
        String inputString1 = JOptionPane.showInputDialog(f, "Input a bit value (0 - 7)");
        int bit = Integer.parseInt(inputString1);



        for(int y = 0; y < timg.getHeight(); y++){
            for(int x = 0; x < timg.getWidth(); x++){
                imageArray1[x][y][1] = ((imageArray1[x][y][1] >> bit) &1)* 255; 
                imageArray1[x][y][2] = ((imageArray1[x][y][2] >> bit) &1)* 255; 
                imageArray1[x][y][3] = ((imageArray1[x][y][3] >> bit) &1)* 255; 
            }
        }

        return convertToBimage(imageArray1);
    }



    //*****************
    //  HISTOGRAMS
    //*****************

    public int[][] findHistogram(BufferedImage timg) {
        int [][][] imageArray1 = convertToArray(timg);

        //Histogram Arrays
        int[] HistogramR = new int[256];
        int[] HistogramG = new int[256];
        int[] HistogramB = new int[256];

        for(int k=0; k<256; k++){ // Initialisation
            HistogramR[k] = 0;
            HistogramG[k] = 0;
            HistogramB[k] = 0;
        }

        for(int y=0; y<timg.getHeight(); y++){ // bin histograms
            for(int x=0; x<timg.getWidth(); x++){
                int r = imageArray1[x][y][1]; //r
                int g = imageArray1[x][y][2]; //g
                int b = imageArray1[x][y][3]; //b
                HistogramR[r]++;
                HistogramG[g]++;
                HistogramB[b]++;
            }
        }

        //pass to normalisation method
        return new int[][] {HistogramR, HistogramG, HistogramB};
    }
    
    public void histogramNormalistaion(BufferedImage timg) {
        int [][] normalised = findHistogram(timg);

        //Histogram Arrays RGB
        double[] HistogramR = new double[256];
        double[] HistogramG = new double[256];
        double[] HistogramB = new double[256];

        for(int k=0; k<256; k++){ // Initialisation
            HistogramR[k] = normalised[0][k];
            HistogramG[k] = normalised[1][k];
            HistogramB[k] = normalised[2][k];
        }

        for(int k=0; k<256; k++){
            HistogramR[k] = HistogramR[k] / (timg.getWidth() * timg.getHeight());
            HistogramG[k] = HistogramR[k] / (timg.getWidth() * timg.getHeight());
            HistogramB[k] = HistogramR[k] / (timg.getWidth() * timg.getHeight());
        }


        //Print out RGB Components
        System.out.println("R:");
        for(int k = 0; k< 256; k++){
            if(HistogramR[k] != 0) System.out.println(k+": "+ HistogramR[k]);
        }

        System.out.println("G:");
        for(int k = 0; k< 256; k++){
            if(HistogramG[k] != 0) System.out.println(k+": "+ HistogramG[k]);
        }

        System.out.println("B:");
        for(int k = 0; k< 256; k++){
            if(HistogramB[k] != 0) System.out.println(k+": "+HistogramB[k]);
        }
        

    }


    public BufferedImage histogramEqualisation(BufferedImage timg){
        BufferedImage nImg = new BufferedImage(timg.getWidth(), timg.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster wr = timg.getRaster();
        WritableRaster er = nImg.getRaster();
        int totpix= wr.getWidth()*wr.getHeight();
        int[] histogram = new int[256];

        for (int x = 1; x < wr.getWidth(); x++) {
            for (int y = 1; y < wr.getHeight(); y++) {
                histogram[wr.getSample(x, y, 0)]++;
            }
        }
        
        int[] chistogram = new int[256];
        chistogram[0] = histogram[0];
        for(int i=1;i<256;i++){
            chistogram[i] = chistogram[i-1] + histogram[i];
        }
        
        float[] arr = new float[256];
        for(int i=0;i<256;i++){
            arr[i] =  (float)((chistogram[i]*255.0)/(float)totpix);
        }
        
        for (int x = 0; x < wr.getWidth(); x++) {
            for (int y = 0; y < wr.getHeight(); y++) {
                int nVal = (int) arr[wr.getSample(x, y, 0)];
                er.setSample(x, y, 0, nVal);
            }
        }
        nImg.setData(er);
        return nImg;
    }




    //************************************
    // CONVOLUTION
    //************************************
    // Masks for convolution
    private float[][] averageMask = {{1f, 1f, 1f}, {1f, 1f, 1f}, {1f,1f,1f}};
    private float[][] weightedMask = {{1f,2f,1f},{2f,4f,2f},{1f,2f,1f}};
    private float[][] fourNL = {{0f, -1f, 0f},{-1f, 4f, -1f}, {0f, -1f, 0f}};
    private float[][] eightNL = {{-1f, -1f, -1f},{-1f, 8f, -1f}, {-1f, -1f, -1f}};
    private float[][] fourNLE = {{0f, -1f, 0f},{-1f, 5f, -1f}, {0f, -1f, 0f}};
    private float[][] eightNLE = {{-1f, -1f, -1f},{-1f, 9f, -1f}, {-1f, -1f, -1f}};
    private float[][] robertsOne = {{0f, 0f, 0f},{0f, 0f, -1f}, {0f, 1f, 0f}};
    private float[][] robertsTwo = {{0f, 0f, 0f},{0f, -1f, 0f}, {0f, 0f, 1f}};
    private float[][] sobelX = {{-1f, 0f, 1f},{-2f, 0f, 2f}, {-1f, 0f, 1f}};
    private float[][] sobelY = {{-1f, -2f, -1f},{0f, 0f, 0f}, {1f, 2f, 1f}};

    private float[][] mask = new float[3][3];

    // Convolution
    public int[][][] applyConvolution(BufferedImage timg, float[][] cMask){
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg);
        int width = timg.getWidth();
        int height = timg.getHeight();
        float r, g, b;
        mask = cMask;

        for(int y =1; y<height-1; y++){
            for(int x= 1; x<width-1; x++){
                r = 0; g = 0; b = 0;
                for(int s= -1; s<=1; s++){
                    for(int t= -1; t<=1; t++){
                        r = r+mask[1-s][1-t] * imageArray1[x+s][y+t][1];
                        g = g+mask[1-s][1-t] * imageArray1[x+s][y+t][2];
                        b = b+mask[1-s][1-t] * imageArray1[x+s][y+t][3];
                    }
                }
                imageArray2[x][y][1] = (int) Math.round(Math.abs(r));
                imageArray2[x][y][2] = (int) Math.round(Math.abs(g));
                imageArray2[x][y][3] = (int) Math.round(Math.abs(b));
            }
        }
        return imageArray2;
    }


    // Averaging
    public BufferedImage average(BufferedImage timg){
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg);
        int width = timg.getWidth();
        int height = timg.getHeight();
        float r, g, b;
        mask = averageMask;
        float total = 0;

        for(int row = 0; row<3; row++){
            for(int col = 0; col < 3; col++){
                total += mask[row][col];
            }
        }

        for(int y =1; y<height-1; y++){
            for(int x= 1; x<width-1; x++){
                r = 0;
                g = 0; 
                b = 0;
                for(int s= -1; s<=1; s++){
                    for(int t= -1; t<=1; t++){
                        r = r+mask[1-s][1-t] * imageArray1[x+s][y+t][1];
                        g = g+mask[1-s][1-t] * imageArray1[x+s][y+t][2];
                        b = b+mask[1-s][1-t] * imageArray1[x+s][y+t][3];
                    }
                }
                imageArray2[x][y][1] = (int) Math.round(Math.abs(r/total));
                imageArray2[x][y][2] = (int) Math.round(Math.abs(g/total));
                imageArray2[x][y][3] = (int) Math.round(Math.abs(b/total));
            }
        }
        return shiftRescale(convertToBimage(imageArray2), 0.7f, 1);
    }


    // weighted averaging
    public BufferedImage weightedAverage(BufferedImage timg){
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg);
        int width = timg.getWidth();
        int height = timg.getHeight();
        float r, g, b;
        mask = weightedMask;
        float total = 0;

        for(int row = 0; row<3; row++){
            for(int col = 0; col < 3; col++){
                total += mask[row][col];
            }
        }

        for(int y =1; y<height-1; y++){
            for(int x= 1; x<width-1; x++){
                r = 0;
                g = 0; 
                b = 0;
                for(int s= -1; s<=1; s++){
                    for(int t= -1; t<=1; t++){
                        r = r+mask[1-s][1-t] * imageArray1[x+s][y+t][1];
                        g = g+mask[1-s][1-t] * imageArray1[x+s][y+t][2];
                        b = b+mask[1-s][1-t] * imageArray1[x+s][y+t][3];
                    }
                }
                imageArray2[x][y][1] = (int) Math.round(Math.abs(r/total));
                imageArray2[x][y][2] = (int) Math.round(Math.abs(g/total));
                imageArray2[x][y][3] = (int) Math.round(Math.abs(b/total));
            }
        }
        return shiftRescale(convertToBimage(imageArray2), 0.7f, 1);
    }


    public BufferedImage fourNL(BufferedImage timg){
        int[][][] imageArray2 = applyConvolution(timg, fourNL);
        return shiftRescale(convertToBimage(imageArray2), 0.7f, 1);
    }
    
    public BufferedImage eightNL(BufferedImage timg){
        int[][][] imageArray2 = applyConvolution(timg, eightNL);
        return shiftRescale(convertToBimage(imageArray2), 0.7f, 1);
    }

    public BufferedImage fourNLE(BufferedImage timg){
        int[][][] imageArray2 = applyConvolution(timg, fourNLE);
        return shiftRescale(convertToBimage(imageArray2), 0.7f, 1);
    }

    public BufferedImage eightNLE(BufferedImage timg){
        int[][][] imageArray2 = applyConvolution(timg, eightNLE);
        return shiftRescale(convertToBimage(imageArray2), 0.7f, 1);
    }

    public BufferedImage robertsOne(BufferedImage timg){
        int[][][] imageArray2 = applyConvolution(timg, robertsOne);
        return shiftRescale(convertToBimage(imageArray2), 0.7f, 1);
    }

     public BufferedImage robertsTwo(BufferedImage timg){
        int[][][] imageArray2 = applyConvolution(timg, robertsTwo);
        return shiftRescale(convertToBimage(imageArray2), 0.7f, 1);
    }

     public BufferedImage sobelX(BufferedImage timg){
        int[][][] imageArray2 = applyConvolution(timg, sobelX);
        return shiftRescale(convertToBimage(imageArray2), 0.7f, 1);
    }

    public BufferedImage sobelY(BufferedImage timg){
        int[][][] imageArray2 = applyConvolution(timg, sobelY);
        return shiftRescale(convertToBimage(imageArray2), 0.7f, 1);
    }



    //************************************
    // STATIC FILTERING
    //************************************

    // Salt & Pepper Noise
    public BufferedImage saltAndPepperNoise(BufferedImage timg){
        int[][][] imageArray1 = convertToArray(timg);
        int width = timg.getWidth();
        int height = timg.getHeight();
        int randNum;
        Random rand = new Random();

        for(int y=0; y<height; y++){
            for(int x=0; x<width; x++){
                randNum = rand.nextInt(10);
                if(randNum == 0){
                    imageArray1[x][y][1] = 255;
                    imageArray1[x][y][2] = 255;
                    imageArray1[x][y][3] = 255;
                }
                else if(randNum == 1){
                    imageArray1[x][y][1] = 0;
                    imageArray1[x][y][2] = 0;
                    imageArray1[x][y][3] = 0;
                }
            }
        }
        return convertToBimage(imageArray1);

    }

    // Min Filtering
    public BufferedImage minFiltering(BufferedImage timg){
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg);
        int width = timg.getWidth();
        int height = timg.getHeight();
        int[] windowR = new int[9];
        int[] windowG = new int[9];
        int[] windowB = new int[9];
        int k = 0;

        for(int y=1; y<height-1; y++){
            for(int x=1; x<width-1; x++){
                k = 0;
               for(int s= -1; s<=1; s++){
                    for(int t= -1; t<=1; t++){
                        windowR[k] = imageArray1[x+t][y+s][1];
                        windowG[k] = imageArray1[x+t][y+s][2];
                        windowB[k] = imageArray1[x+t][y+s][3];
                        k++;
                    }
                }
                Arrays.sort(windowR);
                Arrays.sort(windowG);
                Arrays.sort(windowB);
                imageArray2[x][y][1] = windowR[0];
                imageArray2[x][y][2] = windowG[0];
                imageArray2[x][y][3] = windowB[0];
            }
        }
        return convertToBimage(imageArray2);
    }

    // Max Filtering
    public BufferedImage maxFiltering(BufferedImage timg){
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg);
        int width = timg.getWidth();
        int height = timg.getHeight();
        int[] windowR = new int[9];
        int[] windowG = new int[9];
        int[] windowB = new int[9];
        int k = 0;

        for(int y=1; y<height-1; y++){
            for(int x=1; x<width-1; x++){
                k = 0;
               for(int s= -1; s<=1; s++){
                    for(int t= -1; t<=1; t++){
                        windowR[k] = imageArray1[x+t][y+s][1];
                        windowG[k] = imageArray1[x+t][y+s][2];
                        windowB[k] = imageArray1[x+t][y+s][3];
                        k++;
                    }
                }
                Arrays.sort(windowR);
                Arrays.sort(windowG);
                Arrays.sort(windowB);
                imageArray2[x][y][1] = windowR[windowR.length-1];
                imageArray2[x][y][2] = windowG[windowG.length-1];
                imageArray2[x][y][3] = windowB[windowB.length-1];
            }
        }
        return convertToBimage(imageArray2);

    }

    // Mid-Point Filtering
    public BufferedImage midPointFiltering(BufferedImage timg){
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg);
        int width = timg.getWidth();
        int height = timg.getHeight();
        int[] windowR = new int[9];
        int[] windowG = new int[9];
        int[] windowB = new int[9];
        int k = 0;

        for(int y=1; y<height-1; y++){
            for(int x=1; x<width-1; x++){
                k = 0;
               for(int s= -1; s<=1; s++){
                    for(int t= -1; t<=1; t++){
                        windowR[k] = imageArray1[x+t][y+s][1];
                        windowG[k] = imageArray1[x+t][y+s][2];
                        windowB[k] = imageArray1[x+t][y+s][3];
                        k++;
                    }
                }
                Arrays.sort(windowR);
                Arrays.sort(windowG);
                Arrays.sort(windowB);

                imageArray2[x][y][1] = (int)(windowR[0] + windowR[windowR.length-1]) / 2;
                imageArray2[x][y][2] = (int)(windowG[0] + windowG[windowG.length-1]) / 2;
                imageArray2[x][y][3] = (int)(windowB[0] + windowR[windowB.length-1]) / 2;
            }
        }
        return convertToBimage(imageArray2);

    }

    // Median Filtering
    public BufferedImage medianFiltering(BufferedImage timg){
        int[][][] imageArray1 = convertToArray(timg);
        int[][][] imageArray2 = convertToArray(timg);
        int width = timg.getWidth();
        int height = timg.getHeight();
        int[] windowR = new int[9];
        int[] windowG = new int[9];
        int[] windowB = new int[9];
        int k = 0;

        for(int y=1; y<height-1; y++){
            for(int x=1; x<width-1; x++){
                k = 0;
               for(int s= -1; s<=1; s++){
                    for(int t= -1; t<=1; t++){
                        windowR[k] = imageArray1[x+t][y+s][1];
                        windowG[k] = imageArray1[x+t][y+s][2];
                        windowB[k] = imageArray1[x+t][y+s][3];
                        k++;
                    }
                }
                Arrays.sort(windowR);
                Arrays.sort(windowG);
                Arrays.sort(windowB);

                imageArray2[x][y][1] = windowR[4];
                imageArray2[x][y][2] = windowR[4];
                imageArray2[x][y][3] = windowR[4];
            }
        }
        return convertToBimage(imageArray2);

    }




    //************************************
    // THRESHOLDING
    //************************************

    // Simple Thresholding
    public BufferedImage simpleThresholding(BufferedImage timg){
        int[][][] imageArray1 = convertToArray(timg);
     
        int width = timg.getWidth();
        int height = timg.getHeight();
        int threshold = 120;

        for(int y=1; y<height-1; y++){
            for(int x=1; x<width-1; x++){
             if(imageArray1[x][y][1] <= threshold || imageArray1[x][y][2] <= threshold || imageArray1[x][y][3] <= threshold){
                imageArray1[x][y][1] = 0;
                imageArray1[x][y][2] = 0;
                imageArray1[x][y][3] = 0;
             }
             else{
                imageArray1[x][y][1] = 255;
                imageArray1[x][y][2] = 255;
                imageArray1[x][y][3] = 255;
             }
            }
        }
        return convertToBimage(imageArray1);
    }


   




    //************************************
    //  You need to register your functions here
    //************************************
    public void filterImage() {
 
        if (opIndex == lastOp) {
            return;
        }

        lastOp = opIndex;
        switch (opIndex) {

            //original
            case 0: 
                // biFiltered = bi;
                // arrayImagesFiltered.add(biFiltered);
                for(int i = 0; i<arrayImages.size(); i++){
                    arrayImages.get(i);
                }
                return;
          
             
            //negative
            case 1: 
                // biFiltered = ImageNegative(bi);
                // arrayImagesFiltered.add(biFiltered);

                for(int i = 0; i<arrayImages.size(); i++){
                    arrayImagesFiltered.add(ImageNegative(arrayImages.get(i)));
                
                }
                return;

            //rescale without min and max
            case 2: 
                for(int i = 0; i<arrayImages.size(); i++){
                    arrayImagesFiltered.add(rescaleInput(arrayImages.get(i),0));
                }
                return;

            //rescale with min and max
            case 3: 
                for(int i = 0; i<arrayImages.size(); i++){
                    arrayImagesFiltered.add(rescaleInput(arrayImages.get(i),1));
                }
                return;
            
            //addition
            case 4: 
                imageNumberProcess();
                arrayImagesFiltered.add(addtion(arrayImages.get(image1),arrayImages.get(image2)));
                return;
                
            //subtraction
            case 5: 
                imageNumberProcess();
                arrayImagesFiltered.add(subtraction(arrayImages.get(image1),arrayImages.get(image2)));
                return;
            
            //multiplication
            case 6: 
                imageNumberProcess();
                arrayImagesFiltered.add(multiplication(arrayImages.get(image1),arrayImages.get(image2)));
                return;

            //division
            case 7: 
                imageNumberProcess();
                arrayImagesFiltered.add(division(arrayImages.get(image1),arrayImages.get(image2)));
                return;

            //bitwise NOT
            case 8: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(bitwiseNOT(arrayImages.get(i)));
            }
            return;

            //bitwise AND
            case 9: 
                imageNumberProcess();
                arrayImagesFiltered.add(bitwiseAND(arrayImages.get(image1),arrayImages.get(image2)));
                return;

            //bitwise OR
            case 10: 
                imageNumberProcess();
                arrayImagesFiltered.add(bitwiseOR(arrayImages.get(image1),arrayImages.get(image2)));
                return;
            
            //bitwise XOR
            case 11: 
                imageNumberProcess();
                arrayImagesFiltered.add(bitwiseXOR(arrayImages.get(image1),arrayImages.get(image2)));
                return;

            //logarithmic
            case 12: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(logarithmic(arrayImages.get(i)));
            }
            return;
            
            //powerLaw
            case 13: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(powerLaw(arrayImages.get(i)));
            }
            return;

            //random Look-up Table
            case 14: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(randomLookupTable(arrayImages.get(i)));
            }
            return;

            //bit-plane Slicing
            case 15: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(bitPlaneSlicing(arrayImages.get(i)));
            }
            return;

            //histogramNormalistaion
            case 16: 
            for(int i = 0; i<arrayImages.size(); i++){
                histogramNormalistaion(arrayImages.get(i));
            }
            return;
        
            //histogramEqualisation
            case 17: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(histogramEqualisation(arrayImages.get(i)));
            }
            return;

            //convultion average
            case 18: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(average(arrayImages.get(i)));
            }
            return;

            //convultion weighted average
            case 19: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(weightedAverage(arrayImages.get(i)));
            }
            return;

            //4-neighbour Laplacian
            case 20: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(fourNL(arrayImages.get(i)));
            }
            return;

            //8-neighbour Laplacian
            case 21: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(eightNL(arrayImages.get(i)));
            }
            return;

            //4-neighbour Laplacian Enhancement
            case 22: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(fourNLE(arrayImages.get(i)));
            }
            return;

            //8-neighbour Laplacian Enhancement
            case 23: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(eightNLE(arrayImages.get(i)));
            }
            return;

            //Roberts 1
            case 24: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(robertsOne(arrayImages.get(i)));
            }
            return;

            //Roberts 2
            case 25: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(robertsTwo(arrayImages.get(i)));
            }
            return;

            //Sobel X
            case 26: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(sobelX(arrayImages.get(i)));
            }
            return;

            //Sobel Y
            case 27: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(sobelY(arrayImages.get(i)));
            }
            return;

            //Salt & Pepper Noise
            case 28: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(saltAndPepperNoise(arrayImages.get(i)));
            }
            return;

            //Min Filtering
            case 29: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(minFiltering(arrayImages.get(i)));
            }
            return;

            //Max Filtering
            case 30: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(maxFiltering(arrayImages.get(i)));
            }
            return;

            //Mid-Point Filtering
            case 31: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(midPointFiltering(arrayImages.get(i)));
            }
            return;

            //Median Filtering
            case 32: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(medianFiltering(arrayImages.get(i)));
            }
            return;

            //Simple Thresholding
            case 33: 
            for(int i = 0; i<arrayImages.size(); i++){
                arrayImagesFiltered.add(simpleThresholding(arrayImages.get(i)));
            }
            return;

        }
                
 
    }
 

 
     public void actionPerformed(ActionEvent e) {
        
        if(e.getActionCommand().equals("Undo")) {
            int processedImages = arrayImagesFiltered.size();
         
            arrayImagesFiltered.remove(processedImages-1);
            
            repaint();
  
                return;      
        }

    
        JComboBox cb = (JComboBox)e.getSource();
        if (cb.getActionCommand().equals("SetFilter")) {
             setOpIndex(cb.getSelectedIndex());
             repaint();
        }      
             
        else if (cb.getActionCommand().equals("Formats")) {
             String format = (String)cb.getSelectedItem();
             File saveFile = new File("savedimage."+format);
             JFileChooser chooser = new JFileChooser();
             chooser.setSelectedFile(saveFile);
             int rval = chooser.showSaveDialog(cb);
             if (rval == JFileChooser.APPROVE_OPTION) {
                 saveFile = chooser.getSelectedFile();
                 try {
                     ImageIO.write(biFiltered, format, saveFile);
                 } catch (IOException ex) {
                 }
             }
         }
    };
 
    public static void main(String s[]) {
        JFrame f = new JFrame("Image Processing Demo");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        
        Demo de = new Demo();
        f.add("Center", de);


        JComboBox choices = new JComboBox(de.getDescriptions());
        choices.setActionCommand("SetFilter");
        choices.addActionListener(de);

        JComboBox formats = new JComboBox(de.getFormats());
        formats.setActionCommand("Formats");
        formats.addActionListener(de);

        JButton undo = new JButton("Undo");
        undo.setActionCommand("Undo");
        undo.addActionListener(de);
        

        final FileDialog fileDialog = new FileDialog(f, "Select Image");

        JButton selectImageButton = new JButton("Select an Image");

        selectImageButton.addActionListener(new ActionListener() {
            
            //@Override
            public void actionPerformed(ActionEvent e){
                fileDialog.setVisible(true);
                de.setImages(fileDialog.getDirectory() + fileDialog.getFile());
            }

        });

        JPanel panel = new JPanel();

        panel.add(selectImageButton);
        panel.add(new JLabel("Select Function: "));
        panel.add(choices);
        panel.add(new JLabel("Save As: "));
        panel.add(formats);
        panel.add(undo);
        

        f.add("North", panel);
        f.pack();
        f.setVisible(true);
    }
}
