package Jeu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Timer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;

public class JeuMémoire extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JButton[][] buttons;
    private ImageIcon[][] icons;
    private Image[] images;
    private int[] imageIndices;
    private int[][] numbers;
    private int rows = 4;
    private int cols = 4;
    private int numPairs = 8;
    private int remainingAttempts = 18;
    private JLabel attemptsLabel;
    private JLabel timerLabel;
    private Timer timer;
    private int openedCards = 0;
    private int firstX = -1;
    private int firstY = -1;
    private boolean[][] cardsOpened;
    private int secondsElapsed = 0;
    private ArrayList<Float> bestScores = new ArrayList<>();

    public JeuMémoire() {
    	setResizable(false);
        setTitle("Memory Game");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel gamePanel = new JPanel(new GridLayout(rows, cols));
        attemptsLabel = new JLabel("Tentatives restantes: " + remainingAttempts);
        timerLabel = new JLabel("Temps: 0");
        JPanel controlPanel = new JPanel();
        
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(attemptsLabel, BorderLayout.NORTH);
        mainPanel.add(timerLabel, BorderLayout.SOUTH);
        mainPanel.add(controlPanel, BorderLayout.EAST);

        buttons = new JButton[rows][cols];
        icons = new ImageIcon[rows][cols];
        numbers = new int[rows][cols];
        cardsOpened = new boolean[rows][cols];

        // Charge les images
        loadImages();

        // Mélange les indices des images
        shuffleImageIndices();

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
                            if (openedCards == 1 && finalI == firstX && finalJ == firstY) {
                                // Ne fait rien si l'utilisateur clique une deuxième fois sur la même carte
                                return;
                            }
                            timer.start();
                            if (openedCards == 0) {
                                firstX = finalI;
                                firstY = finalJ;
                                buttons[finalI][finalJ].setIcon(icons[finalI][finalJ]);
                                openedCards++;
                            } else if (openedCards == 1) {
                                remainingAttempts--;
                                attemptsLabel.setText("Tentatives restantes: " + remainingAttempts);

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
                                            if (remainingAttempts <= 0) {
                                                timer.stop();
                                                endGame(false);
                                            } else if (checkGameEnd()) {
                                                endGame(false);
                                            }
                                        }
                                    });
                                    flipBackTimer.setRepeats(false);
                                    flipBackTimer.start();
                                }
                                if (remainingAttempts <= 0) {
                                    timer.stop();
                                    endGame(false);
                                }
                            }
                        }
                    }


                });
            }
        }

        JMenuItem newGameMenuItem = new JMenuItem("Nouvelle partie");
        JMenuItem bestScoresMenuItem = new JMenuItem("Meilleurs scores");
        bestScoresMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBestScoresDialog();
            }
        });

        JMenu gameMenu = new JMenu("Options");
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
                loadBestScores();
            }
        });

        setContentPane(mainPanel);
        startNewGame();
    }

    private void loadImages() {
        images = new Image[numPairs];
        for (int i = 0; i < numPairs; i++) {
            try {
                images[i] = ImageIO.read(new File("src/Jeu/img/img" + i + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erreur lors du chargement de l'image : img" + i + ".png");
            }
        }
    }


    private void shuffleImageIndices() {
        imageIndices = new int[numPairs * 2];
        for (int i = 0; i < numPairs; i++) {
            imageIndices[i * 2] = i;
            imageIndices[i * 2 + 1] = i;
        }
        // Mélange les indices
        for (int i = 0; i < imageIndices.length; i++) {
            int randomIndex = (int) (Math.random() * imageIndices.length);
            int temp = imageIndices[i];
            imageIndices[i] = imageIndices[randomIndex];
            imageIndices[randomIndex] = temp;
        }
    }

    private void startNewGame() {
        attemptsLabel.setText("Tentatives restantes: " + remainingAttempts);

        // Réinitialise le compteur de secondes avant de démarrer le timer
        secondsElapsed = 0;
        timerLabel.setText("Temps: 0");

        if (timer != null) {
            timer.stop();
        }

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secondsElapsed++;
                timerLabel.setText("Temps: " + secondsElapsed);
            }
        });

        generateIcons(); // Generation des icones
        resetCardsOpened(); // Réinitialise l'état d'ouverture des cartes
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
            JOptionPane.showMessageDialog(this, "Bravo ! Vous avez gagné !\n En: " + secondsElapsed + " secondes");
            while (bestScores.size() > 3) {
                bestScores.remove(bestScores.size() - 1);
            }
            saveBestScores();
        } else {
            JOptionPane.showMessageDialog(this, "Game over! Vous avez perdu!");
        }
    }


    private void saveBestScores() {
        try {
            File file = new File("bestScores.txt");
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            for (Float score : bestScores) {
                bw.write(score.toString());
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadBestScores() {
        File file = new File("bestScores.txt");
        if (file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String line;
                bestScores.clear(); // Nettoie la liste avant de charger les nouveaux scores
                while ((line = br.readLine()) != null) {
                    bestScores.add(Float.parseFloat(line));
                }
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showBestScoresDialog() {
        loadBestScores();
        StringBuilder scoresText = new StringBuilder("Meilleurs Scores:\n");
        for (Float score : bestScores) {
            scoresText.append(score).append(" secondes\n");
        }
        JOptionPane.showMessageDialog(this, scoresText.toString(), "Meilleurs Scores", JOptionPane.INFORMATION_MESSAGE);
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