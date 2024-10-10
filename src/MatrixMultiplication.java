import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class MatrixMultiplication {
    static int NUM_ROWS = 3;
    static int NUM_COL = 3;
    static Integer[][] matrixA = new Integer[NUM_ROWS][NUM_COL];
    static Integer[][] matrixB = new Integer[NUM_ROWS][NUM_COL];
    static Integer[][] result = new Integer[NUM_ROWS][NUM_COL];

    static class Multiplication{
        int row;
        int col;
        future fut;

        public Multiplication(int row, int col, future fut){
            this.row = row;
            this.col = col;
            this.fut = fut;
        }

        public int calculate(){
            int answer = 0;
            for(int i = 0; i < matrixA[row].length; i++){
                answer += matrixA[row][i] * matrixB[i][col];
            }
            return answer;
        }
    }

    static class future{
        boolean finished;

        public synchronized void get() throws InterruptedException {
            if(!finished)
                wait();
        }

        public synchronized void finish(){
            this.finished = true;
            notifyAll();
        }
    }

    static class MyThread extends Thread{
        LinkedBlockingQueue<Multiplication> linkedBlockingQueue;
        Thread[] workers;

        public MyThread(int number){
            linkedBlockingQueue = new LinkedBlockingQueue<>();
            workers = new Thread[number];

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    while(true){
                        if(!linkedBlockingQueue.isEmpty()){
                            try {
                                Multiplication multiplication = linkedBlockingQueue.take();
                                int ans = multiplication.calculate();
                                result[multiplication.row][multiplication.col] = ans;
                                multiplication.fut.finish();
                            } catch (InterruptedException e) {

                            }

                        }
                    }
                }
            };

            for(int i = 0; i < workers.length; i++){
                workers[i] = new Thread(runnable);
            }

            for(int i = 0; i < workers.length; i++){
                workers[i].start();
            }

            finishAll();
        }

        public future addTask(int row, int col){
            future fut = new future();
            Multiplication multiplication = new Multiplication(row, col, fut);
            try {
                linkedBlockingQueue.put(multiplication);
            } catch (InterruptedException _) {

            }
            return fut;
        }

        public void finishAll(){
            for (Thread worker : workers) {
                worker.interrupt();
            }
        }

    }

    public static void main(String[] args) {
        MyThread myThread = new MyThread(5);
        Random random = new Random();
        for(int i = 0; i < NUM_ROWS; i++){
            for(int j = 0; j < NUM_COL; j++){
                matrixA[i][j] =random.nextInt(10);
            }
        }

        for(int i = 0; i < NUM_ROWS; i++){
            for(int j = 0; j < NUM_COL; j++){
                matrixB[i][j] =random.nextInt(10);
            }
        }

        for(int i = 0; i < NUM_ROWS; i++){
            for(int j = 0; j < NUM_COL; j++){
                System.out.print(matrixA[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();

        for(int i = 0; i < NUM_ROWS; i++){
            for(int j = 0; j < NUM_COL; j++){
                System.out.print(matrixB[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
        future[] futures = new future[NUM_ROWS * NUM_COL];
        for(int i = 0; i < NUM_ROWS; i++){
            for(int j = 0; j < NUM_COL; j++){
                futures[i + NUM_COL * j] = myThread.addTask(i, j);
            }
        }

        for(var f : futures){
            try{
                f.get();
            } catch (InterruptedException e) {
                System.out.println("...");
            }
        }

        for(int i = 0; i < NUM_ROWS; i++){
            for(int j = 0; j < NUM_COL; j++){
                System.out.print(result[i][j] + " ");
            }
            System.out.println();
        }
    }
}
