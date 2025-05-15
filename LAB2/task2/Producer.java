package task2;

import java.util.Random;

public class Producer implements Runnable {
    private Drop drop;
    private int[] numbers;

    public Producer(Drop drop, int[] numbers) {
        this.drop = drop;
        this.numbers = numbers;
    }

    public void run() {
        Random random = new Random();
        for (int number : numbers) {
            drop.put(number);
            try {
                Thread.sleep(random.nextInt(10)); // невелика затримка
            } catch (InterruptedException e) {}
        }
        drop.put(-1); // маркер завершення
    }
}

