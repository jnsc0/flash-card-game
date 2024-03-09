import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlashCard extends JFrame {
    private int score = 0;
    private int timeLimitInSeconds = 60;
    private boolean gameRunning = true;
    private boolean[] combinationsShown = new boolean[13 * 13 * 4];
    private Random random = new Random();
    private JLabel scoreLabel;
    private JLabel questionLabel;
    private JTextField answerField;
    private JLabel timerLabel;
    private Timer timer;
    private JButton showCombinationsButton;

    private List<QuestionInfo> storedQuestions = new ArrayList<>();

    public FlashCard() {
        setTitle("Flash Card Game");
        setSize(500, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        scoreLabel = new JLabel("Score: " + score);
        questionLabel = new JLabel();
        answerField = new JTextField();
        JButton submitButton = new JButton("Submit");
        timerLabel = new JLabel("Time Left: " + timeLimitInSeconds);
        JButton restartButton = new JButton("Restart");
        showCombinationsButton = new JButton("Show Combinations");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));
        panel.add(timerLabel);
        panel.add(scoreLabel);
        panel.add(questionLabel);
        panel.add(answerField);
        panel.add(restartButton);
        panel.add(submitButton);
        panel.add(showCombinationsButton);

        //when click button, submit answer
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processAnswer();
            }
        });

        //when click enter, submit answer.
        answerField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processAnswer();
            }
        });

        //when click button, restart game
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                endGame();
            }
        });

        //when click button, show past combinations
        showCombinationsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAllCombinations();
            }
        });

        add(panel);

        initializeGame();
    }

    private void initializeGame() {
        askQuestion();

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTimer();
            }
        });
        timer.start();
    }

    private void updateTimer() {
        if (timeLimitInSeconds > 0) {
            timeLimitInSeconds--;
            timerLabel.setText("Time Left: " + timeLimitInSeconds);
        } else {
            endGame();
        }
    }

    private void askQuestion() {
        // Check if all combinations have been shown
        if (allCombinationsShown()) {
            resetCombinationsShown();
        }

        int num1, num2, operation;

        do {
            num1 = random.nextInt(13);
            num2 = random.nextInt(13);
            operation = random.nextInt(4);
        } while (combinationsShown[(num1 * 13 + num2) * 4 + operation]);

        combinationsShown[(num1 * 13 + num2) * 4 + operation] = true;

        scoreLabel.setText("Score: " + score);
        setQuestionLabel(num1, num2, operation);

        // store the questions
        storeQuestion(num1, num2, operation);

        answerField.setText(""); 
    }

    private boolean allCombinationsShown() {
        for (boolean combinationShown : combinationsShown) {
            if (!combinationShown) {
                return false;
            }
        }
        return true;
    }

    private void resetCombinationsShown() {
        for (int i = 0; i < combinationsShown.length; i++) {
            combinationsShown[i] = false;
        }
        JOptionPane.showMessageDialog(this, "All combinations shown. Restarting the game.");
    }


    private void setQuestionLabel(int num1, int num2, int operation) {
        switch (operation) {
            case 0:
                questionLabel.setText(num1 + " + " + num2 + " = ");
                break;
            case 1:
                questionLabel.setText(num1 + " - " + num2 + " = ");
                break;
            case 2:
                questionLabel.setText(num1 + " * " + num2 + " = ");
                break;
            case 3:
                questionLabel.setText(num1 + " / " + num2 + " = ");
                break;
        }
    }

    private void processAnswer() {
        double userAnswer;
        try {
            userAnswer = Double.parseDouble(answerField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
            return;
        }

        int num1 = extractNum1FromQuestionLabel();
        int num2 = extractNum2FromQuestionLabel();
        int operation = extractOperationFromQuestionLabel();

        if (checkAnswer(num1, num2, userAnswer, operation)) {
            score++;
        } else {
            score--;
        }

        askQuestion();
    }

    private int extractNum1FromQuestionLabel() {
        String[] parts = questionLabel.getText().split(" ");
        return Integer.parseInt(parts[0]);
    }

    private int extractNum2FromQuestionLabel() {
        String[] parts = questionLabel.getText().split(" ");
        return Integer.parseInt(parts[2]);
    }

    private int extractOperationFromQuestionLabel() {
        String[] parts = questionLabel.getText().split(" ");
        switch (parts[1]) {
            case "+":
                return 0;
            case "-":
                return 1;
            case "*":
                return 2;
            case "/":
                return 3;
            default:
                return -1;
        }
    }

    private boolean checkAnswer(int num1, int num2, double userAnswer, int operation) {
        double correctAnswer = calculateCorrectAnswer(num1, num2, operation);
        if (userAnswer == correctAnswer) {
            JOptionPane.showMessageDialog(this, "Correct!");
            return true;
        } else {
            if (correctAnswer % 1 == 0) {
                JOptionPane.showMessageDialog(this, "Incorrect. The correct answer is " + (int) correctAnswer);
            } else {
                String formattedAnswer = String.format("%.1f", correctAnswer);
                JOptionPane.showMessageDialog(this, "Incorrect. The correct answer is " + formattedAnswer);
            }
            return false;
        }
    }

    private double calculateCorrectAnswer(int num1, int num2, int operation) {
        switch (operation) {
            case 0:
                return num1 + num2;
            case 1:
                return num1 - num2;
            case 2:
                return num1 * num2;
            case 3:
                if (num1 == 0 || num2 == 0) {
                    return 0;
                } else {
                    return (double) num1 / num2;
                }
            default:
                return 0;
        }
    }

    private void endGame() {
        gameRunning = false;
        timer.stop();
        JOptionPane.showMessageDialog(this, "Time's up! Game Over.\nYour final score is: " + score + "\n Click OK to restart");
        resetGame();
    }

    private void resetGame() {
        score = 0;
        timeLimitInSeconds = 60;
        gameRunning = true;
        timerLabel.setText("Time Left: " + timeLimitInSeconds);
        scoreLabel.setText("Score: " + score);
        initializeGame();
    }

    private void storeQuestion(int num1, int num2, int operation) {
        storedQuestions.add(new QuestionInfo(num1, num2, operation));
    }

    private void showAllCombinations() {
        int combinationCount = storedQuestions.size();
        StringBuilder combinations = new StringBuilder("All Stored Questions (" + combinationCount + " combinations):\n");
    
        for (int i = 0; i < combinationCount; i++) {
            QuestionInfo questionInfo = storedQuestions.get(i);
            String operator = getOperatorSymbol(questionInfo.getOperation());
            combinations.append("Combination ").append(i + 1).append(": ")
                         .append(questionInfo.getNum1()).append(" ").append(operator).append(" ").append(questionInfo.getNum2()).append("\n");
        }
    
        JOptionPane.showMessageDialog(this, combinations.toString(), "All Stored Questions", JOptionPane.INFORMATION_MESSAGE);
    }
    

    private String getOperatorSymbol(int operation) {
        switch (operation) {
            case 0:
                return "+";
            case 1:
                return "-";
            case 2:
                return "*";
            case 3:
                return "/";
            default:
                return "";
        }
    }

    private static class QuestionInfo {
        private final int num1;
        private final int num2;
        private final int operation;

        public QuestionInfo(int num1, int num2, int operation) {
            this.num1 = num1;
            this.num2 = num2;
            this.operation = operation;
        }

        public int getNum1() {
            return num1;
        }

        public int getNum2() {
            return num2;
        }

        public int getOperation() {
            return operation;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FlashCard().setVisible(true);
            }
        });
    }
}
