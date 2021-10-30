package testimagefilter;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.IIOException;

import javax.imageio.ImageIO;
import java.util.Arrays;




public class TestImageFilter {

	public static void main(String[] args) throws Exception {
		
		BufferedImage image = null;
		String srcFileName = null;
                
		try {
			srcFileName = args[0];
                       
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
	
		int[] srcSql = image.getRGB(0, 0, w, h, null, 0, w);
		int[] srcPrll = image.getRGB(0, 0, w, h, null, 0, w);
		int[] dstSql = new int[srcSql.length];
		int[] dstPrll = new int[srcPrll.length];
                
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Available processors: "+Integer.toString(processors)+"\n");
                
//sequential start---------------------------------------------------
     	System.out.println("Starting sequential image filter...");

		long startTimeSql = System.currentTimeMillis();
		ImageFilter filterSql = new ImageFilter(srcSql, dstSql, w, h);
		filterSql.apply();
		long endTimeSql = System.currentTimeMillis();

		long tSequential = endTimeSql - startTimeSql; 
		System.out.println("Sequential image filter took " + tSequential + " milliseconds.");

        BufferedImage dstImageSql = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		dstImageSql.setRGB(0, 0, w, h, dstSql, 0, w);

        String dstNameSql = "sqlFiltered" + srcFileName;
		File dstFileSql = new File(dstNameSql);
		ImageIO.write(dstImageSql, "JPG", dstFileSql);
		System.out.println("Output image: " + dstNameSql+"\n");	
//sequential end-------------------------------------------------------
                
                
//parallel start-------------------------------------------------------
                
        ParallelFJImageFilter filterPrll = new ParallelFJImageFilter(srcPrll, dstPrll, w, h);
        long startTimePrll = filterPrll.startTimeP;
        long endTimePrll = filterPrll.endTimeP;
        long tParallel = endTimePrll - startTimePrll;
        int totalThreadUsed = filterPrll.totalThreads;
        System.out.println("Parallel image filter took " + tParallel + " milliseconds using "+totalThreadUsed+" threads.");
                
                
		BufferedImage dstImagePrll = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		dstImagePrll.setRGB(0, 0, w, h, dstPrll, 0, w);

        String dstName = "pFiltered" + srcFileName;
		File dstFilePrll = new File(dstName);
		ImageIO.write(dstImagePrll, "JPG", dstFilePrll);
		System.out.println("Output image: " + dstName);
//parallel end---------------------------------------------------------
                
                
//image verification---------------------------------------------------

        if(Arrays.equals(dstSql, dstPrll)){
            System.out.println("Output image verified successfully!");
        }else{
            System.out.println("Output image not verified!");
        }
               
//speed up compersion--------------------------------------------------
        float speedUp = (float)tSequential/tParallel;
        if(speedUp >= 0.7*totalThreadUsed){
            System.out.println("Speedup: "+speedUp+" ok ( >= "+0.7*totalThreadUsed+" )");
        }else{
            System.out.println("Speedup: "+speedUp+" not ok ( < "+0.7*totalThreadUsed+" )");
        }
                    

	}
}
