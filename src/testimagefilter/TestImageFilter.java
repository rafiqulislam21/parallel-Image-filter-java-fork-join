package testimagefilter;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.IIOException;

import javax.imageio.ImageIO;

import java.util.concurrent.ForkJoinPool;


public class TestImageFilter {

	public static void main(String[] args) throws Exception {
		
		BufferedImage image = null;
		String srcFileName = null;
                String baseImgPath = null;
                String imageName = "IMAGE1.JPG";
                
		try {
//			srcFileName = args[0];
                        baseImgPath = new File("").getAbsolutePath()+"\\img\\";

                        srcFileName = baseImgPath + imageName;
			File srcFile = new File(srcFileName);
			image = ImageIO.read(srcFile);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Usage: java TestAll <image-file>");
			System.exit(1);
		}
		catch (IIOException e) {
			System.out.println("Error reading image file " + srcFileName + " !");
			System.exit(1);
		}

		System.out.println("Source image: " + srcFileName);

		int w = image.getWidth();
		int h = image.getHeight();
		System.out.println("Image size is " + w + "x" + h);
		System.out.println();
	
		int[] src = image.getRGB(0, 0, w, h, null, 0, w);
		int[] dst = new int[src.length];

		/*
                System.out.println("Starting sequential image filter.");

		long startTime = System.currentTimeMillis();
		ImageFilter filter0 = new ImageFilter(src, dst, w, h);
		filter0.apply();
		long endTime = System.currentTimeMillis();

		long tSequential = endTime - startTime; 
		System.out.println("Sequential image filter took " + tSequential + " milliseconds.");
*/
                //parallel-------------------------------
                System.out.println("Starting parallel image filter.");

                int processors = Runtime.getRuntime().availableProcessors();
                System.out.println(Integer.toString(processors) + " processor"
                        + (processors != 1 ? "s are " : " is ")
                        + "available");
                
                ForkJoinPool pool = ForkJoinPool.commonPool();
                ParallelFJImageFilter task = new ParallelFJImageFilter(src, dst, w, h, 0, 100);

//                ForkJoinPool pool = new ForkJoinPool();

                long startTime = System.currentTimeMillis();
                pool.invoke(task);
                long endTime = System.currentTimeMillis();

		long tSequential = endTime - startTime; 
		System.out.println("Parallel image filter took " + tSequential + " milliseconds.");
                //parallel-------------------------------
                
		BufferedImage dstImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		dstImage.setRGB(0, 0, w, h, dst, 0, w);

//		String dstName = "Filtered" + srcFileName;
                
                String dstName = baseImgPath + "Filtered" + imageName;
		File dstFile = new File(dstName);
		ImageIO.write(dstImage, "JPG", dstFile);

		System.out.println("Output image: " + dstName);	
	}
}
