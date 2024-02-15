package Jeu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.Timer;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class JeuMémoire extends JFrame {

    private JButton[][] buttons;
    private ImageIcon[][] icons;
    private Image[] images; // Stocke les images originales
    private int[] imageIndices; // Stocke les indices des images pour les paires
    private int[][] numbers;
    private int rows = 4;
    private int cols = 4;
    private int numPairs = 8;
    private int remainingAttempts = 18;
    private JLabel attemptsLabel;
    private JLabel timerLabel;
    private Chrono timer;
    private int openedCards = 0;
    private int firstX = -1;
    private int firstY = -1;
    private boolean[][] cardsOpened;
    private ArrayList<Float> bestScores = new ArrayList<>();

    public JeuMémoire() {
    	setResizable(false);
        setTitle("Memory Game");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel gamePanel = new JPanel(new GridLayout(rows, cols));
        attemptsLabel = new JLabel("Attempts Left: " + remainingAttempts);
        timerLabel = new JLabel("Time: 0");
        JPanel controlPanel = new JPanel();
        
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(attemptsLabel, BorderLayout.NORTH);
        mainPanel.add(timerLabel, BorderLayout.SOUTH);
        mainPanel.add(controlPanel, BorderLayout.EAST);

        buttons = new JButton[rows][cols];
        icons = new ImageIcon[rows][cols];
        numbers = new int[rows][cols];
        cardsOpened = new boolean[rows][cols];

        // Charger les images
        loadImages();

        // Mélanger les indices des images
        shuffleImageIndices();

        // Initialiser les boutons et assigner les paires d'images
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setPreferredSize(new Dimension(80, 80));
                gamePanel.add(buttons[i][j]);
                int finalI = i;
                int finalJ = j;
                buttons[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!cardsOpened[finalI][finalJ] && remainingAttempts >= 0) {
                            if (openedCards == 0) {
                                firstX = finalI;
                                firstY = finalJ;
                                buttons[finalI][finalJ].setIcon(icons[finalI][finalJ]);
                                openedCards++;
                            } else if (openedCards == 1) {
                                remainingAttempts--;
                                if (remainingAttempts >= 0) {
                                    attemptsLabel.setText("Attempts Left: " + remainingAttempts);
                                }
                                timer.stop();
                                timer = new Chrono(timerLabel);
                                timer.start();
                                buttons[finalI][finalJ].setIcon(icons[finalI][finalJ]);
                                if (numbers[firstX][firstY] == numbers[finalI][finalJ]) {
                                    cardsOpened[firstX][firstY] = true;
                                    cardsOpened[finalI][finalJ] = true;
                                    openedCards = 0;
                                    if (checkGameEnd()) {
                                        endGame(true);
                                    }
                                } else {
                                    openedCards++;
                                    Timer flipBackTimer = new Timer(1000, new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            buttons[firstX][firstY].setIcon(null);
                                            buttons[finalI][finalJ].setIcon(null);
                                            openedCards = 0;
                                            if (checkGameEnd()) {
                                                endGame(false);
                                            }
                                        }
                                    });
                                    flipBackTimer.setRepeats(false);
                                    flipBackTimer.start();
                                }
                            }
                        }
                    }
                });
            }
        }

        JMenuItem newGameMenuItem = new JMenuItem("New Game");
        JMenuItem bestScoresMenuItem = new JMenuItem("Best Scores");
        JMenu gameMenu = new JMenu("Game");
        gameMenu.add(newGameMenuItem);
        gameMenu.add(bestScoresMenuItem);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        newGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        });

        bestScoresMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBestScores();
            }
        });

        setContentPane(mainPanel);
        startNewGame();
    }

    private void loadImages() {
        images = new Image[numPairs];
        for (int i = 0; i < numPairs; i++) {
            try {
                String imagePath = "src/Jeu/img/img" + i + ".png";
                images[i] = ImageIO.read(new File(imagePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void shuffleImageIndices() {
        imageIndices = new int[numPairs * 2];
        for (int i = 0; i < numPairs; i++) {
            imageIndices[i * 2] = i;
            imageIndices[i * 2 + 1] = i;
        }
        // Mélanger les indices
        for (int i = 0; i < imageIndices.length; i++) {
            int randomIndex = (int) (Math.random() * imageIndices.length);
            int temp = imageIndices[i];
            imageIndices[i] = imageIndices[randomIndex];
            imageIndices[randomIndex] = temp;
        }
    }

    private void startNewGame() {
        attemptsLabel.setText("Attempts Left: " + remainingAttempts);
        if (timer != null) {
            timer.stop();
        }
        timer = new Chrono(timerLabel);
        timer.start();
        generateIcons();
        resetCardsOpened();
    }

    private void generateIcons() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int index = i * cols + j;
                numbers[i][j] = imageIndices[index];
                icons[i][j] = new ImageIcon(images[imageIndices[index]]);
            }
        }
    }

    private void resetCardsOpened() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cardsOpened[i][j] = false;
                buttons[i][j].setIcon(null);
            }
        }
    }

    private boolean checkGameEnd() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!cardsOpened[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void endGame(boolean won) {
        timer.stop();
        if (won) {
            JOptionPane.showMessageDialog(this, "Congratulations! You won!\nTime taken: " + timer.getTimeElapsed());
            float timeElapsed = timer.getTimeElapsedSeconds();
            bestScores.add(timeElapsed);
            Collections.sort(bestScores);
            while (bestScores.size() > 3) {
                bestScores.remove(bestScores.size() - 1);
            }
            saveBestScores();
        } else {
            JOptionPane.showMessageDialog(this, "Game over! You lost!");
        }
    }

    private void saveBestScores() {
        // Code pour enregistrer les meilleurs scores dans un fichier
    }

    private void showBestScores() {
        // Code pour afficher les meilleurs scores
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JeuMémoire().setVisible(true);
            }
        });
    }
}
