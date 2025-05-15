package task2;

import java.util.Random;

public class Consumer implements Runnable {
    private Drop drop;

    public Consumer(Drop drop) {
        this.drop = drop;
    }

    public void run() {
        Random random = new Random();
        int number;
        while ((number = drop.take()) != -1) {
            System.out.println("NUMBER RECEIVED: " + number);
            try {
                Thread.sleep(random.nextInt(10));
            } catch (InterruptedException e) {}
        }
    }
}

