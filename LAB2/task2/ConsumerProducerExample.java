package task2;

public class ConsumerProducerExample {
    public static void main(String[] args) {
        int size = 5000; 
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = i + 1;
        }

        Drop drop = new Drop();
        new Thread(new Producer(drop, data)).start();
        new Thread(new Consumer(drop)).start();
    }
}
