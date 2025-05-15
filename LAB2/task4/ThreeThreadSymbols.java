package task4;

public class ThreeThreadSymbols {    private static final Object lock = new Object();
    private static int turn = 0; // 0 = |, 1 = \, 2 = /

    public static void main(String[] args) {
        Thread pipeThread = new Thread(() -> printSymbol('|', 0));
        Thread backslashThread = new Thread(() -> printSymbol('\\', 1));
        Thread slashThread = new Thread(() -> printSymbol('/', 2));

        pipeThread.start();
        backslashThread.start();
        slashThread.start();

        try {
            pipeThread.join();
            backslashThread.join();
            slashThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printSymbol(char symbol, int symbolTurn) {
        for (int i = 0; i < 90; i++) {
            synchronized (lock) {
                while (turn != symbolTurn) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                System.out.print(symbol);
                turn = (turn + 1) % 3;
                lock.notifyAll();
            }
        }
    }
}
