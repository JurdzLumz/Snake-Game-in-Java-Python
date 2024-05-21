import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class SnakeGame extends JFrame implements ActionListener, KeyListener {
    private static final int GAME_WIDTH = 600;
    private static final int GAME_HEIGHT = 400;
    private static final int SPACE_SIZE = 50;
    private static final int LARGE_SPACE_SIZE = 70;
    private static final int BODY_PARTS = 3;
    private static final int INITIAL_SPEED = 120;
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    private int speed = INITIAL_SPEED;
    private int score = 0;
    private int foodCounter = 0;
    private boolean paused = false;
    private boolean gameOver = false;
    private String direction = "down";

    private JLabel scoreLabel;
    private JLabel highScoreLabel;
    private JLabel gameOverLabel;
    private JPanel gamePanel;
    private JButton startButton;
    private JButton tryAgainButton;
    private JButton pauseButton;
    private Timer timer;
    private Snake snake;
    private Food food;

    public static void main(String[] args) {
        new SnakeGame();
    }

    public SnakeGame() {
        setTitle("Snake Game");
        setSize(GAME_WIDTH, GAME_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        scoreLabel = new JLabel("Score: 0", JLabel.CENTER);
        scoreLabel.setFont(new Font("Consolas", Font.BOLD, 40));
        add(scoreLabel, BorderLayout.NORTH);

        highScoreLabel = new JLabel("High Score: 0", JLabel.CENTER);
        highScoreLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        add(highScoreLabel, BorderLayout.SOUTH);

        gameOverLabel = new JLabel("", JLabel.CENTER);
        gameOverLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        add(gameOverLabel, BorderLayout.CENTER);
        gameOverLabel.setVisible(false);

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

                if (snake != null) {
                    g.setColor(Color.GREEN);
                    for (int[] coordinate : snake.getCoordinates()) {
                        g.fillRect(coordinate[0], coordinate[1], SPACE_SIZE, SPACE_SIZE);
                    }
                }

                if (food != null) {
                    if (food.isLarge()) {
                        g.setColor(Color.RED);
                        g.fillOval(food.getX(), food.getY(), LARGE_SPACE_SIZE, LARGE_SPACE_SIZE);
                    } else {
                        g.setColor(Color.YELLOW);
                        g.fillOval(food.getX(), food.getY(), SPACE_SIZE, SPACE_SIZE);
                    }
                }

                if (gameOver) {
                    g.setColor(Color.RED);
                    g.setFont(new Font("Consolas", Font.BOLD, 70));
                    g.drawString("YOU LOSE", GAME_WIDTH / 4, GAME_HEIGHT / 2);
                }
            }
        };
        gamePanel.setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        add(gamePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start");
        startButton.setFont(new Font("Consolas", Font.BOLD, 20));
        startButton.addActionListener(e -> startGame());
        buttonPanel.add(startButton);

        tryAgainButton = new JButton("Try Again");
        tryAgainButton.setFont(new Font("Consolas", Font.BOLD, 20));
        tryAgainButton.addActionListener(e -> startGame());
        buttonPanel.add(tryAgainButton);
        tryAgainButton.setVisible(false);

        pauseButton = new JButton("Pause");
        pauseButton.setFont(new Font("Consolas", Font.BOLD, 20));
        pauseButton.addActionListener(e -> togglePause());
        buttonPanel.add(pauseButton);

        add(buttonPanel, BorderLayout.SOUTH);

        addKeyListener(this);
        setFocusable(true);
        setLocationRelativeTo(null);
        setVisible(true);

        loadHighScore();
    }

    private void startGame() {
        score = 0;
        foodCounter = 0;
        direction = "down";
        paused = false;
        gameOver = false;
        speed = INITIAL_SPEED;
        scoreLabel.setText("Score: " + score);
        tryAgainButton.setVisible(false);
        gameOverLabel.setVisible(false);
        snake = new Snake();
        food = new Food();
        repaint();

        timer = new Timer(speed, this);
        timer.start();
    }

    private void togglePause() {
        paused = !paused;
        if (!paused) {
            timer.start();
        } else {
            timer.stop();
        }
    }

    private void loadHighScore() {
        try {
            File file = new File(HIGH_SCORE_FILE);
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                if (scanner.hasNextInt()) {
                    int highScore = scanner.nextInt();
                    highScoreLabel.setText("High Score: " + highScore);
                }
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveHighScore() {
        try {
            FileWriter writer = new FileWriter(HIGH_SCORE_FILE);
            writer.write(String.valueOf(score));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gameOver() {
        timer.stop();
        int currentHighScore = Integer.parseInt(highScoreLabel.getText().split(": ")[1]);
        if (score > currentHighScore) {
            highScoreLabel.setText("High Score: " + score);
            saveHighScore();
        }
        gameOver = true;
        gameOverLabel.setText("Game Over! Your score: " + score + ". High Score: " + highScoreLabel.getText().split(": ")[1]);
        gameOverLabel.setVisible(true);
        gamePanel.repaint();
        tryAgainButton.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused) {
            int[] head = snake.getCoordinates().get(0);
            int newX = head[0];
            int newY = head[1];

            switch (direction) {
                case "up":
                    newY -= SPACE_SIZE;
                    break;
                case "down":
                    newY += SPACE_SIZE;
                    break;
                case "left":
                    newX -= SPACE_SIZE;
                    break;
                case "right":
                    newX += SPACE_SIZE;
                    break;
            }

            if (newX < 0 || newX >= GAME_WIDTH || newY < 0 || newY >= GAME_HEIGHT || snake.collidesWith(newX, newY)) {
                gameOver();
                return;
            }

            snake.move(newX, newY);

            if (food.isEaten(newX, newY)) {
                if (food.isLarge()) {
                    score += 3;
                    speed = Math.max(speed - 10, 10);  // Increase speed by reducing delay
                    timer.setDelay(speed);  // Update the timer with the new speed
                } else {
                    score += 1;
                }
                foodCounter++;
                scoreLabel.setText("Score: " + score);

                if (foodCounter % 5 == 0) {
                    food = new Food(true);
                } else {
                    food = new Food();
                }
            } else {
                snake.shrink();
            }

            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_LEFT:
                if (!direction.equals("right")) direction = "left";
                break;
            case KeyEvent.VK_RIGHT:
                if (!direction.equals("left")) direction = "right";
                break;
            case KeyEvent.VK_UP:
                if (!direction.equals("down")) direction = "up";
                break;
            case KeyEvent.VK_DOWN:
                if (!direction.equals("up")) direction = "down";
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }

    class Snake {
        private ArrayList<int[]> coordinates;

        public Snake() {
            coordinates = new ArrayList<>();
            for (int i = 0; i < BODY_PARTS; i++) {
                coordinates.add(new int[]{0, 0});
            }
        }

        public ArrayList<int[]> getCoordinates() {
            return coordinates;
        }

        public void move(int x, int y) {
            coordinates.add(0, new int[]{x, y});
        }

        public void shrink() {
            coordinates.remove(coordinates.size() - 1);
        }

        public boolean collidesWith(int x, int y) {
            for (int i = 1; i < coordinates.size(); i++) {
                int[] part = coordinates.get(i);
                if (part[0] == x && part[1] == y) {
                    return true;
                }
            }
            return false;
        }
    }

    class Food {
        private int x;
        private int y;
        private boolean isLarge;

        public Food() {
            this(false);
        }

        public Food(boolean isLarge) {
            this.isLarge = isLarge;
            Random rand = new Random();
            int size = isLarge ? LARGE_SPACE_SIZE : SPACE_SIZE;
            x = rand.nextInt(GAME_WIDTH / size) * size;
            y = rand.nextInt(GAME_HEIGHT / size) * size;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean isLarge() {
            return isLarge;
        }

        public boolean isEaten(int snakeX, int snakeY) {
            int size = isLarge ? LARGE_SPACE_SIZE : SPACE_SIZE;
            return snakeX >= x && snakeX < x + size && snakeY >= y && snakeY < y + size;
        }
    }
}
