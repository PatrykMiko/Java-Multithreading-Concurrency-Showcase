# Java Multithreading & Concurrency Showcase

![Java](https://img.shields.io/badge/Java-11%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white) ![Concurrency](https://img.shields.io/badge/Multithreading-Concurrent-E34F26?style=for-the-badge)

This project explores the power of **Java Multithreading** through two practical, real-world examples: rendering an animated GUI application and performing highly efficient concurrent network I/O operations.

## 🚀 Key Modules

### 1. Animated Analog Clock (`ClockWithGui.java`)
A Java Swing application that draws a fully functional analog clock. 
* Uses `Graphics2D` and `AffineTransform` matrix math to calculate continuous, smooth rotation for the hour, minute, and second hands.
* Demonstrates separating the UI rendering (Event Dispatch Thread) from the background time-keeping thread using `Thread.sleep()`.

### 2. Concurrent File Downloader (`DownloadExample.java`)
A robust network utility script designed to showcase the massive performance benefits of parallel execution. It downloads multiple PDF files from a university server, demonstrating the evolution of threading techniques:
* **Sequential Approach:** Slow, blocks the thread until each file finishes.
* **Concurrent V1:** Fire-and-forget threads (flawed timing).
* **Concurrent V2 (Atomic Variables):** Uses `AtomicInteger` and "busy waiting" (`Thread.yield()`). Safe, but CPU-intensive.
* **Concurrent V3 (Semaphores):** The optimal solution. Uses `java.util.concurrent.Semaphore` to block the main thread with zero CPU overhead until all worker threads signal completion.

## 🛠️ Technology Stack
* **Java 17+**
* **Concurrency Utilities:** `Thread`, `Runnable`, `Semaphore`, `AtomicInteger`
* **Java 2D Graphics:** `java.awt.geom.AffineTransform`, `Graphics2D`
* **Network I/O:** `java.net.URL`, `InputStream`, `FileOutputStream`

## 💻 Code Highlight: Semaphore Synchronization

This snippet demonstrates the optimal way to wait for multiple spawned threads to finish downloading files before proceeding:

```java
// Main Thread blocks efficiently without consuming CPU
try {
    sem.acquire(filesToDownload.length);
} catch (InterruptedException e) {
    throw new RuntimeException(e);
}

// Inside the worker Thread (Downloader)
// ... I/O operations ...
sem.release(); // Signals the main thread that this task is done
