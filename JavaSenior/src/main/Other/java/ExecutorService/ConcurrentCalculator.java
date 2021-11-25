package ExecutorService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

//并行计算数组的和
public class ConcurrentCalculator {
    private ExecutorService exec;
    private int cpuCoreNumber;
    private List<FutureTask<Long>> tasks = new ArrayList<FutureTask<Long>>();

    // 内部类
    class SumCalculator implements Callable<Long> {
        private int[] number;
        private int start;
        private int end;

        private SumCalculator(final int[] number, int start, int end) {
            this.number = number;
            this.start = start;
            this.end = end;
        }

        @Override
        public Long call() throws Exception {
            Long sum = 0l;
            for (int i = start; i < end; i++) {
                sum += number[i];
            }
            return sum;
        }
    }

    public ConcurrentCalculator() {
        cpuCoreNumber = Runtime.getRuntime().availableProcessors();
        exec = Executors.newFixedThreadPool(cpuCoreNumber);
    }

    public Long sum(final int[] number) {
        // 根据CPU核心个数拆分任务，创建FutureTask并提交到Executor
        for (int i = 0; i < cpuCoreNumber; i++) {
            int intcreament = number.length / cpuCoreNumber + 1;
            int start = intcreament * i;
            int end = intcreament * i + intcreament;
            if (end > number.length)
                end = number.length;
            SumCalculator subCalc = new SumCalculator(number, start, end);
            FutureTask<Long> task = new FutureTask<>(subCalc);
            tasks.add(task);
            if (!exec.isShutdown()) {
                exec.submit(task);
            }
        }
        return getResult();
    }


    /**
     * 迭代每个只任务，获得部分和，相加返回
     *
     * @return
     */
    public Long getResult() {
        Long result = 0l;
        for (FutureTask<Long> task : tasks) {
            try {
                // 如果计算未完成则阻塞
                Long subSum = task.get();
                result += subSum;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
            return result;
        }

        public void close() {
            exec.shutdown();
        }
}
