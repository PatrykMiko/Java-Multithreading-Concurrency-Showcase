package org.example;

import java.time.LocalTime;

/**
 * A simple console-based clock that runs on a separate thread.
 * Demonstrates basic Thread extension and the sleep() mechanism.
 */
public class Clock extends Thread {

    @Override
    public void run() {
        // Infinite loop to continuously update the time
        for (;;) {
            LocalTime time = LocalTime.now();
            System.out.printf("%02d:%02d:%02d\n",
                    time.getHour(),
                    time.getMinute(),
                    time.getSecond());
            try {
                // Pause the thread for 1 second (1000 milliseconds)
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        // Start the thread (invokes the run() method asynchronously)
        new Clock().start();
    }
}
