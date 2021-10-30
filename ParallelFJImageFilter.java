package testimagefilter;

import static java.util.concurrent.ForkJoinTask.invokeAll;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Md Rafiqul Islam
 */
public class ParallelFJImageFilter{
    private int[] src;
    private int[] dst;
    private int width;
    private int height;
    
    private ReentrantLock lock = new ReentrantLock();
    private final int NRSTEPS = 100;  
    int totalThreads = 2;
    int Threshold;
    // int Threshold = 1000;
    
    long startTimeP;   
    long endTimeP;
    
    public ParallelFJImageFilter(int[] src, int[] dst, int w, int h) {
    	this.src = src;
	    this.dst = dst;
	    this.width = w;
	    this.height = h;
        
        Threshold = width/totalThreads;
        // System.out.println("width===="+width);
        // System.out.println("threads used===="+totalThreads);
        // System.out.println("threshold===="+Threshold);
        // Threshold = 1000;
        apply(totalThreads);
    }
    
    public void apply(int nthreads) {
        ForkJoinPool pool = new ForkJoinPool(nthreads);
        System.out.println("Starting parallel image filter using "+Integer.toString(nthreads)+" threads...");
        InnerFJImageFilter task = new InnerFJImageFilter(src, dst, 1, height);
        startTimeP = System.currentTimeMillis();
        pool.invoke(task);
        endTimeP = System.currentTimeMillis();
    }
    
   
  
    
    class InnerFJImageFilter extends RecursiveAction{
        private int[] src;
        private int[] dst;

        int start;
        int end;
    
        public InnerFJImageFilter(int[] src, int[] dst, int start, int end) {
            this.src = src;
            this.dst = dst;

            this.start = start;
            this.end = end;
        }
        
        public void filterProcess() {
        
            int index, pixel;
            for (int steps = 0; steps < NRSTEPS; steps++) {
                for (int i = start; i < end - 1; i++) {
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
                try{
                    lock.lock();
                    int[] help; 
                    help = src; 
                    src = dst;
                    dst = help;
                }finally{
                    lock.unlock();
                }
                            
                        
            }
        }
    
        @Override
        protected void compute() {
            
            int size = end-start;
            // System.out.println("size here------------"+size);
             if (size < Threshold) {
                filterProcess();
            }else{
              int mid = (end+start) / 2;
              InnerFJImageFilter task1 = new InnerFJImageFilter(src, dst, start, mid);
              InnerFJImageFilter task2 = new InnerFJImageFilter(src, dst, mid, end);
              ForkJoinTask.invokeAll(task1,task2);
          }
      }
  }
}