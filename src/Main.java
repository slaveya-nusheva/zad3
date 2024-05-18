import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Product implements Comparable<Product> {
    String name;
    String type;
    double price;

    public Product(String name, String type, double price) {
        this.name = name;
        this.type = type;
        this.price = price;
    }

    @Override
    public int compareTo(Product other) {
        return Double.compare(this.price, other.price);
    }

    @Override
    public String toString() {
        return name + " " + type + " " + price;
    }
}

class FileReadTask implements Runnable {
    private String fileName;
    private PriorityQueue<Product> queue;
    private Lock lock;

    public FileReadTask(String fileName, PriorityQueue<Product> queue, Lock lock) {
        this.fileName = fileName;
        this.queue = queue;
        this.lock = lock;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                String name = parts[0];
                String type = parts[1];
                double price = Double.parseDouble(parts[2]);
                Product product = new Product(name, type, price);
                lock.lock();
                try {
                    queue.offer(product);
                } finally {
                    lock.unlock();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        PriorityQueue<Product> priorityQueue = new PriorityQueue<>();
        Lock lock = new ReentrantLock();

        Thread guessThread = new Thread(new FileReadTask("guess.txt", priorityQueue, lock));
        Thread calvinKleinThread = new Thread(new FileReadTask("calvinklein.txt", priorityQueue, lock));
        Thread trussardiThread = new Thread(new FileReadTask("trussardi.txt", priorityQueue, lock));

        guessThread.start();
        calvinKleinThread.start();
        trussardiThread.start();

        try {
            guessThread.join();
            calvinKleinThread.join();
            trussardiThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter("top10products.txt")) {
            for (int i = 0; i < 10 && !priorityQueue.isEmpty(); i++) {
                writer.write(priorityQueue.poll().toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
