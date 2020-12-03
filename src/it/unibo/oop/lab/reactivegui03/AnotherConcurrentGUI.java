package it.unibo.oop.lab.reactivegui03;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This is a first example on how to realize a reactive GUI.
 */
public final class AnotherConcurrentGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private static final int SECONDS_TO_WAIT = 10;

    private final JLabel display = new JLabel();
    private final JButton stop = new JButton("stop");
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final CountingAgent countingAgent = new CountingAgent();
    private final InterferingAgent interferingAgent = new InterferingAgent();

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        up.addActionListener(this);
        stop.addActionListener(this);
        down.addActionListener(this);
        up.setEnabled(false);
        this.getContentPane().add(panel);
        this.setVisible(true);
        new Thread(countingAgent).start();
        new Thread(interferingAgent).start();
        }

    public void actionPerformed(final ActionEvent ev) {
        Object src = ev.getSource();
        if (src == up) {
            countingAgent.invert();
            up.setEnabled(false);
            down.setEnabled(true);
        } else if (src == stop) {
            countingAgent.stopCounting();
            up.setEnabled(false);
            down.setEnabled(false);
            stop.setEnabled(false);
        } else if (src == down) {
            countingAgent.invert();
            up.setEnabled(true);
            down.setEnabled(false);
        }
    }
    private class InterferingAgent implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(SECONDS_TO_WAIT * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            up.setEnabled(false);
            down.setEnabled(false);
            stop.setEnabled(false);
            countingAgent.stopCounting();
        }

    }
    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private class CountingAgent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         * 
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         * 
         * For more details on how to use volatile:
         * 
         * http://archive.is/4lsKW
         * 
         */
        private volatile boolean stop;
        private int counter;
        private volatile boolean decrementing;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    /*
                     * All the operations on the GUI must be performed by the
                     * Event-Dispatch Thread (EDT)!
                     */
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(Integer.toString(CountingAgent.this.counter)));
                    if (this.decrementing) {
                        this.counter--;
                    } else {
                        this.counter++;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        public void invert() {
            this.decrementing = !this.decrementing;
        }
    }
}
