package filter;

import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;
import static java.util.concurrent.ForkJoinTask.invokeAll;

public class ParallelFJImageFilter {

    
    private static final int NRSTEPS = 100;
    public static int threshold;
    long totalTime;

    private int[] src;
    private int[] dst;
    private int width;
    private int height;

    public ParallelFJImageFilter(int[] src, int[] dst, int width, int height) {
        this.src = src;
        this.dst = dst;
        this.width = width;
        this.height = height;
   }

    public void apply(int nThreads) {
        threshold = (width)/nThreads;
        
        ForkJoinPool pool = new ForkJoinPool(nThreads);
        InnerParallelFJImageFilter filter1;
        final long startTime = System.currentTimeMillis();
        for (int steps = 0; steps < NRSTEPS; steps++) {
            filter1 = new InnerParallelFJImageFilter(src, dst, 1, height - 1);
            pool.invoke(filter1);

            // swap references
            int[] help; help = src; src = dst; dst = help;
        }
        final long endTime = System.currentTimeMillis();

        totalTime = endTime - startTime;

    }

    class InnerParallelFJImageFilter extends RecursiveAction{
        private int[] src;
        private int[] dst;

        int start;
        int end;
    
        public InnerParallelFJImageFilter(int[] src, int[] dst, int start, int end) {
            this.src = src;
            this.dst = dst;
            this.start = start;
            this.end = end;
        }
        
        public void filterProcess() {
            int index, pixel;
            for (int i = start; i < end; i++) {
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
            
        }
    
        @Override
        protected void compute() {
            if (end - start < threshold) {
                filterProcess();
            } else {
                double preciseMiddle = (start + end) / 2.0;
                int middle = (int) Math.round(preciseMiddle / 10.0) * 10;
                InnerParallelFJImageFilter task1 = new InnerParallelFJImageFilter(src, dst, start, middle);
                InnerParallelFJImageFilter task2 = new InnerParallelFJImageFilter(src, dst, middle, end);
    
                invokeAll(task1, task2);
            }
        }
  }
}
