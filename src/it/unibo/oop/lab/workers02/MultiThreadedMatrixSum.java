package it.unibo.oop.lab.workers02;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadedMatrixSum implements SumMatrix {

    private final int nthread;

    public MultiThreadedMatrixSum(final int nthread) {
        super();
        this.nthread = nthread;
    }

    private static class Worker extends Thread {

        private final double[][] matrix;
        private final int startpos;
        private final int nelem;
        private long res;

        Worker(final double[][] matrix, final int start, final int size) {
            super();
            this.matrix = matrix;
            this.startpos = start;
            this.nelem = size;
        }

        @Override
        public void run() {
            System.out.println("Working from position " + startpos + " to position " + (startpos + nelem - 1));
            for (int i = startpos; i < matrix.length && i < startpos + nelem; i++) {
                for (var x : matrix[i]) {
                    this.res += x;
                }
            }
        }

        public double getResult() {
            return res;
        }

    }

    @Override
    public double sum(final double[][] matrix) {
        final int size = matrix.length % this.nthread + matrix.length / this.nthread;
        final List<Worker> workers = new ArrayList<>(this.nthread);
        for (int start = 0; start < matrix.length; start += size) {
            workers.add(new Worker(matrix, start, size));
        }
        for (final Worker w: workers) {
            w.start();
        }
        double sum = 0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return sum;
    }

}
