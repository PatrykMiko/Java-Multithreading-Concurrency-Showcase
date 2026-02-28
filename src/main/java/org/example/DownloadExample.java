package org.example;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates various approaches to handling I/O operations (file downloading)
 * using multithreading. Compares sequential execution with concurrent execution
 * using different synchronization mechanisms.
 */
public class DownloadExample {

    // Thread-safe counter to track completed downloads
    static AtomicInteger count = new AtomicInteger(0);
    // Semaphore used for the most efficient thread synchronization (v3)
    static Semaphore sem = new Semaphore(0);

    static String [] toDownload = {
            "https://home.agh.edu.pl/~pszwed/wyklad-c/01-jezyk-c-intro.pdf",
            "https://home.agh.edu.pl/~pszwed/wyklad-c/02-jezyk-c-podstawy-skladni.pdf",
            "https://home.agh.edu.pl/~pszwed/wyklad-c/03-jezyk-c-instrukcje.pdf",
            "https://home.agh.edu.pl/~pszwed/wyklad-c/04-jezyk-c-funkcje.pdf",
            "https://home.agh.edu.pl/~pszwed/wyklad-c/05-jezyk-c-deklaracje-typy.pdf",
            "https://home.agh.edu.pl/~pszwed/wyklad-c/06-jezyk-c-wskazniki.pdf",
            "https://home.agh.edu.pl/~pszwed/wyklad-c/07-jezyk-c-operatory.pdf",
            "https://home.agh.edu.pl/~pszwed/wyklad-c/08-jezyk-c-lancuchy-znakow.pdf",
            "https://home.agh.edu.pl/~pszwed/wyklad-c/09-jezyk-c-struktura-programow.pdf",
            "https://home.agh.edu.pl/~pszwed/wyklad-c/10-jezyk-c-dynamiczna-alokacja-pamieci.pdf",
            "https://home.agh.edu.pl/~pszwed/wyklad-c/11-jezyk-c-biblioteka-we-wy.pdf",
            "https://home.agh.edu.pl/~pszwed/wyklad-c/preprocesor-make-funkcje-biblioteczne.pdf",
    };

    public static void main(String[] args) {
        System.out.println("Sequential download");
        sequentialDownload();

        // System.out.println("\nConcurrent download");
        // concurrentDownload();

        // System.out.println("\nConcurrent download v2");
        // concurrentDownloadv2();

        System.out.println("\nConcurrent download v3");
        concurrentDownloadv3();
    }

    /**
     * Downloads files one by one on the main thread. Very slow for multiple files.
     */
    static void sequentialDownload() {
        double t1 = System.nanoTime() / 1e6;
        for (String url : toDownload) {
            new Downloader(url).run(); // Blocks until the download finishes
        }
        double t2 = System.nanoTime() / 1e6;
        System.out.printf(Locale.US, "t2-t1=%f ms\n", t2 - t1);
    }

    /**
     * Starts multiple threads but fails to measure time correctly because
     * the main thread doesn't wait for worker threads to finish.
     */
    static void concurrentDownload() {
        double t1 = System.nanoTime() / 1e6;
        for (String url : toDownload) {
            Thread thread = new Thread(new Downloader(url));
            thread.start();
        }
        double t2 = System.nanoTime() / 1e6;
        System.out.printf(Locale.US, "t2-t1=%f ms (Inaccurate!)\n", t2 - t1);
    }

    /**
     * Uses an AtomicInteger to count finished threads and 'Thread.yield()'
     * to wait. This is known as "busy waiting" and is CPU intensive (anti-pattern).
     */
    static void concurrentDownloadv2() {
        double t1 = System.nanoTime() / 1e6;
        for (String url : toDownload) {
            Thread thread = new Thread(new Downloader(url));
            thread.start();
        }
        // Busy waiting loop
        while (count.get() != toDownload.length) {
            Thread.yield();
        }
        double t2 = System.nanoTime() / 1e6;
        System.out.printf(Locale.US, "t2-t1=%f ms\n", t2 - t1);
    }

    /**
     * The optimal approach. Uses a Semaphore to block the main thread
     * efficiently without consuming CPU cycles until all downloads are complete.
     */
    static void concurrentDownloadv3() {
        double t1 = System.nanoTime() / 1e6;
        for (String url : toDownload) {
            Thread thread = new Thread(new Downloader(url));
            thread.start();
        }
        try {
            // Wait until the semaphore is released 'N' times by the worker threads
            sem.acquire(toDownload.length);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        double t2 = System.nanoTime() / 1e6;
        System.out.printf(Locale.US, "t2-t1=%f ms\n", t2 - t1);
    }

    /**
     * Runnable task responsible for downloading a single file over HTTP
     * and saving it locally.
     */
    static class Downloader implements Runnable {
        private final String url;

        Downloader(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            String fileName = url.substring(url.lastIndexOf('/') + 1);

            // try-with-resources ensures streams are closed automatically
            try (InputStream in = new URL(url).openStream();
                 FileOutputStream out = new FileOutputStream(fileName)) {

                // Read byte by byte (could be optimized with a byte buffer)
                for (;;) {
                    int c = in.read();
                    if (c < 0) {
                        break;
                    }
                    out.write(c);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Done: " + fileName);

            // Notify tracking mechanisms
            count.incrementAndGet(); // Used by v2
            sem.release();           // Used by v3
        }
    }
}
