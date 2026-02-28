package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.time.LocalTime;

/**
 * An analog clock GUI that uses Java2D to render the clock face and hands.
 * It uses a background thread to trigger UI repaints every second.
 */
public class ClockWithGui extends JPanel {

    public ClockWithGui() {
        // Start the background thread responsible for updating the clock
        ClockThread clockThread = new ClockThread();
        clockThread.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smoother lines and text
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Move the origin (0,0) to the center of the panel
        g2d.translate(getWidth() / 2, getHeight() / 2);

        // Draw the outer circle of the clock
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(-150, -150, 2 * 150, 2 * 150);

        FontMetrics fm = g2d.getFontMetrics();

        // Draw the clock numbers (1 to 12) using AffineTransform for rotation
        for (int i = 1; i < 13; i++) {
            AffineTransform at = new AffineTransform();
            at.rotate(2 * Math.PI / 12 * i);
            Point2D src = new Point2D.Float(0, -120);
            Point2D trg = new Point2D.Float();
            at.transform(src, trg);

            String text = Integer.toString(i);

            // Adjust text position to center it precisely
            int x = (int) trg.getX() - (fm.stringWidth(text) / 2);
            int y = (int) trg.getY() + (fm.getAscent() / 3);

            g2d.drawString(text, x, y);
        }

        LocalTime time = LocalTime.now();
        int second = time.getSecond();
        int minute = time.getMinute();
        int hour = time.getHour();

        // --- Draw Hour Hand ---
        AffineTransform saveAT = g2d.getTransform();
        // Calculate continuous rotation including minute progress
        g2d.rotate((hour % 12 + minute / 60.0) * (2 * Math.PI / 12));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, 0, 0, -75);
        g2d.setTransform(saveAT); // Restore previous transform

        // --- Draw Minute Hand ---
        AffineTransform saveAT2 = g2d.getTransform();
        g2d.rotate((minute + second / 60.0) * (2 * Math.PI / 60));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(0, 0, 0, -115);
        g2d.setTransform(saveAT2);

        // --- Draw Second Hand ---
        AffineTransform saveAT3 = g2d.getTransform();
        g2d.rotate(second * (2 * Math.PI / 60));
        g2d.setColor(Color.red);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(0, 0, 0, -130);
        g2d.setTransform(saveAT3);

        // Draw the center pin
        g2d.setColor(Color.BLACK);
        g2d.fillOval(-5, -5, 10, 10);

        // Draw the minute/second tick marks along the edge
        for (int i = 0; i < 60; i++) {
            AffineTransform save = g2d.getTransform();
            g2d.rotate(i * (2 * Math.PI / 60));

            if (i % 5 == 0) {
                // Hour marker (thicker)
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(Color.BLACK);
                g2d.drawLine(0, -150, 0, -150 + 15);
            } else {
                // Minute/Second marker (thinner)
                g2d.setStroke(new BasicStroke(1));
                g2d.setColor(Color.GRAY);
                g2d.drawLine(0, -150, 0, -150 + 5);
            }
            g2d.setTransform(save);
        }
    }

    /**
     * Background thread responsible for triggering the UI update.
     * Keeps the clock synchronized with the system time.
     */
    class ClockThread extends Thread {
        @Override
        public void run() {
            while (true) {
                LocalTime t = LocalTime.now();
                System.out.printf("%02d:%02d:%02d\n", t.getHour(), t.getMinute(), t.getSecond());

                repaint(); // Schedules a call to paintComponent() on the EDT
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void main(String[] args) {
        // Ensure GUI creation is done on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Clock");
            frame.setContentPane(new ClockWithGui());
            frame.setSize(700, 700);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);
            frame.setVisible(true);
        });
    }
}
