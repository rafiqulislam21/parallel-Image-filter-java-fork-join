package filter;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class TestImageFilter {

    private static final int[] threadList = { 1, 2, 4, 8, 16, 32 };
    private static final double[] speedUpVal = { 0.7, 1.4, 2.8, 5.6, 11.2, 0.0 };

    private static String sourceFilePath = null;
    private static String sourceFileName = null;
    private static String srcFileName = null;

    private static long timeSequential;
    private static long timeParallel;

    public static void main(String[] args) throws Exception {
        
        BufferedImage image = null;
        try {
            srcFileName = args[0];
            File srcFile = new File(srcFileName);
            sourceFilePath = srcFile.getAbsolutePath();
            sourceFileName = srcFile.getName();
            image = ImageIO.read(srcFile);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: java TestAll <image-file>");
            System.exit(1);
        } catch (IIOException e) {
            System.out.println("Error reading image file " + srcFileName + " !");
            System.exit(1);
        }

        System.out.println("Source image: " + srcFileName);

        int width = image.getWidth();
        int height = image.getHeight();
        System.out.println("Image size is " + width + "x" + height);

//sequential part start----------------------------------------------------------
        int[] sequentialSrc = image.getRGB(0, 0, width, height, null, 0, width);
        int[] sequentialDst = new int[sequentialSrc.length];

        runSequentialFilter(sequentialSrc, sequentialDst, width, height);
//sequential part end------------------------------------------------------------

//parallel part start------------------------------------------------------------
        System.out.println("\nAvailable processors: " + Runtime.getRuntime().availableProcessors());

        for (int i = 0; i < threadList.length; i++) {
            // Reset the source and destination for new index
            int[] parallelSrc = image.getRGB(0, 0, width, height, null, 0, width);
            int[] parallelDst = new int[parallelSrc.length];

            runParallelFilter(parallelSrc, parallelDst, width, height, threadList[i]);


            verifyImageFilter(sequentialDst, parallelDst);
            displaySpeedUp(speedUpVal[i]);

        }
//parallel part end------------------------------------------------------------
    }



//sequential filter, function---------------------------------------------------
private static void runSequentialFilter(int[] src, int[] dst, final int width, final int height) throws IOException {
    System.out.println("\nStarting sequential image filter....");

    final long startTime = System.currentTimeMillis();
    final ImageFilter filter0 = new ImageFilter(src, dst, width, height);
    filter0.apply();
    final long endTime = System.currentTimeMillis();

    final long totalTime = endTime - startTime;
    System.out.println("Sequential image filter took " + totalTime + " milliseconds.");
    timeSequential = totalTime;

    BufferedImage dstImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    dstImage.setRGB(0, 0, width, height, dst, 0, width);

    String filteredFileName = sourceFilePath.replace(sourceFileName, "Filtered" + sourceFileName);
    final File dstFile = new File(filteredFileName);

    ImageIO.write(dstImage, "jpg", dstFile);

    System.out.println("Output image: " + dstFile.getName());
}


//parallel filter, function-----------------------------------------------------
    private static void runParallelFilter(int[] src, int[] dst, final int width, final int height, final int numOfThreads) throws IOException {
        System.out.println("\nStarting parallel image filter using " + numOfThreads + " threads....");


        ParallelFJImageFilter filter1 = new ParallelFJImageFilter(src, dst, width, height);
        filter1.apply(numOfThreads);
        long totalTime = filter1.totalTime;
        timeParallel = totalTime;
        System.out.println("Parallel image filter took " + totalTime + " milliseconds using " + numOfThreads + " threads.");
        
        final BufferedImage dstImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        dstImage.setRGB(0, 0, width, height, dst, 0, width);

        final String filteredFileName = sourceFilePath.replace(sourceFileName, "ParallelFiltered" + sourceFileName);
        final File dstFile = new File(filteredFileName);

        ImageIO.write(dstImage, "jpg", dstFile);

        System.out.println("Output image (parallel filter): " + dstFile.getName());
    }

//verify parallel filter image and sequential image, function--------------------------------------
    private static void verifyImageFilter(int[] source, int[] destination) {
        boolean isValid = Arrays.equals(source, destination);

        if (isValid)
            System.out.println("Output image verified successfully!");
        else
            System.out.println("Output image verification failed!");
    }

//compare parallel efficiency with sequential, function--------------------------------------------
    private static void displaySpeedUp(double speedSequential) {
        double speedUp = (double) timeSequential / timeParallel;

        if (speedSequential > 0) {
            if(speedUp >= speedSequential){
                System.out.println("Speedup: " + speedUp + " ok (>= " + speedSequential + ")");
            }else{
                System.out.println("Speedup: " + speedUp +  " not ok (>= " + speedSequential + ")");
            }
        } else {
            System.out.println("Speedup: " + speedUp);
        }
    }

}
