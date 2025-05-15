package task4;

public class SimpleSymbolsPrinter {    public static void main(String[] args) {
        Thread pipeThread = new Thread(() -> printSymbol('|'));
        Thread backslashThread = new Thread(() -> printSymbol('\\'));
        Thread slashThread = new Thread(() -> printSymbol('/'));

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

    private static void printSymbol(char symbol) {
        for (int i = 0; i < 90; i++) {
            System.out.print(symbol);
            try {
                Thread.sleep((int)(Math.random() * 5));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

