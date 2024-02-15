package Jeu;

import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class Chrono {
    private int seconds;
    private Timer timer;
    private JLabel label;

    public Chrono(JLabel label) {
        this.label = label;
        this.seconds = 0;
        this.label.setText("Time: 0");
        this.timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seconds++;
                label.setText("Time: " + seconds);
            }
        });
    }

    public void start() {
        this.timer.start();
    }

    public void stop() {
        this.timer.stop();
    }

    public int getTimeElapsed() {
        return seconds;
    }

    public float getTimeElapsedSeconds() {
        return (float) seconds / 1000;
    }
}
