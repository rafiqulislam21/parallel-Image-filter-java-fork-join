/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testimagefilter;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

//import java.util.concurrent.ForkJoinPool;
//import java.util.concurrent.RecursiveTask;

/**
 *
 * @author ribij
 */
public class ParallelFJImageFilter extends RecursiveAction{
    private int[] src;
    private int[] dst;
    private int width;
    private int height;
    
    private int start;
    private int end;

    private final int NRSTEPS = 100;  
    protected static int Threshold = 10;
    
    public ParallelFJImageFilter(int[] src, int[] dst, int w, int h, int start, int end) {
    	this.src = src;
	this.dst = dst;
	width = w;
	height = h;
        
        this.start = start;
        this.end = end;
    }

    public void apply() {
            int index, pixel;
            for (int steps = start; steps < end; steps++) {
                    for (int i = 1; i < height - 1; i++) {
                            for (int j = 1; j < width - 1; j++) {
                                    float rt = 0, gt = 0, bt = 0;
                                    for (int k = i - 1; k <= i + 1; k++) {
                                            index = k * width + j - 1;
                                            pixel = src[index];
                                            rt += (float) ((pixel & 0x00ff0000) >> 16);
                                            gt += (float) ((pixel & 0x0000ff00) >> 8);
                                            bt += (float) ((pixel & 0x000000ff));
                                                    index = k * width + j;
                                            pixel = src[index];
                                            rt += (float) ((pixel & 0x00ff0000) >> 16);
                                            gt += (float) ((pixel & 0x0000ff00) >> 8);
                                            bt += (float) ((pixel & 0x000000ff));
                                                    index = k * width + j + 1;
                                            pixel = src[index];
                                            rt += (float) ((pixel & 0x00ff0000) >> 16);
                                            gt += (float) ((pixel & 0x0000ff00) >> 8);
                                            bt += (float) ((pixel & 0x000000ff));
                                    }
                                    // Re-assemble destination pixel.
                                    index = i * width + j;
                                    int dpixel = (0xff000000) | (((int) rt / 9) << 16) | (((int) gt / 9) << 8) | (((int) bt / 9));
                                    dst[index] = dpixel;
                            }
                    }
                    // swap references
                    int[] help; help = src; src = dst; dst = help;
            }
    }
    
    
    @Override
    protected void compute() {
        int size = end-start;
        if (size < Threshold) {
            apply();
//            return;
        }else{
            int mid = (end+start) / 2;
            ParallelFJImageFilter task1 = new ParallelFJImageFilter(src, dst, height, width, start, mid);
            ParallelFJImageFilter task2 = new ParallelFJImageFilter(src, dst, height, width, mid, end);
            invokeAll(task1,task2);
        }
    }
}
