import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Random;

public class RockPaperScissorsGUI extends JFrame {
    private JLabel rockLabel, paperLabel, scissorsLabel, resultLabel, scoreLabel;
    private String playerName;
    private int playerScore, computerScore;

    public RockPaperScissorsGUI(String playerName) {
        this.playerName = playerName;
        this.playerScore = 0;
        this.computerScore = 0;
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Rock Paper Scissors Game");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 3, 10, 10)); // 2 rows, 3 columns, 10px horizontal and vertical gap

        rockLabel = new JLabel(new ImageIcon(resizeImage("/Users/chrisackermann/Downloads/rock.png", 80, 80)));
        paperLabel = new JLabel(new ImageIcon(resizeImage("/Users/chrisackermann/Downloads/paper.png", 80, 80)));
        scissorsLabel = new JLabel(new ImageIcon(resizeImage("/Users/chrisackermann/Downloads/scissors.png", 80, 80)));
        resultLabel = new JLabel("Result: ");
        scoreLabel = new JLabel("Score: " + playerScore + " (You) - " + computerScore + " (Computer)");

        panel.add(rockLabel);
        panel.add(paperLabel);
        panel.add(scissorsLabel);
        panel.add(resultLabel);
        panel.add(scoreLabel);
        panel.add(new JLabel()); // Empty label for spacing

        rockLabel.addMouseListener(new LabelListener("Rock"));
        paperLabel.addMouseListener(new LabelListener("Paper"));
        scissorsLabel.addMouseListener(new LabelListener("Scissors"));

        add(panel);

        setVisible(true);

        // Check if the player exists in the database and add them if they don't

    }

    private void checkAndAddPlayer() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/demo", "root", "bailey");
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_stats WHERE name = ?");
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                // Player does not exist in the database, add them
                PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO player_stats (name, wins, losses) VALUES (?, 0, 0)");
                insertStatement.setString(1, playerName);
                insertStatement.executeUpdate();
            }

            connection.close();
        } catch (SQLException ex) {
        }
    }

    private class LabelListener extends MouseAdapter {
        private String playerSelection;

        public LabelListener(String playerSelection) {
            this.playerSelection = playerSelection;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            String computerSelection = generateComputerChoice();
            String result = determineWinner(playerSelection, computerSelection);
            resultLabel.setText("Result: " + result);
            scoreLabel.setText("Score: " + playerScore + " (You) - " + computerScore + " (Computer)");

            // Show a popup message with the computer's choice
            JOptionPane.showMessageDialog(null, "Computer chose: " + computerSelection);
        }
    }

    private String generateComputerChoice() {
        Random rand = new Random();
        int choice = rand.nextInt(3);
        switch (choice) {
            case 0:
                return "Rock";
            case 1:
                return "Paper";
            default:
                return "Scissors";
        }
    }

    private String determineWinner(String player, String computer) {
        String result;
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/demo", "root", "bailey20030909");
            PreparedStatement statement = connection.prepareStatement("UPDATE player_stats SET wins = wins + ?, losses = losses + ? WHERE name = ?");
            if (player.equals(computer)) {
                // It's a tie
                statement.setInt(1, 0);
                statement.setInt(2, 0);
                result = "It's a tie!";
            } else if ((player.equals("Rock") && computer.equals("Scissors")) ||
                    (player.equals("Paper") && computer.equals("Rock")) ||
                    (player.equals("Scissors") && computer.equals("Paper"))) {
                // Player wins
                statement.setInt(1, 1);
                statement.setInt(2, 0);
                playerScore++;
                result = "You win!";
            } else {
                // Computer wins
                statement.setInt(1, 0);
                statement.setInt(2, 1);
                computerScore++;
                result = "Computer wins!";
            }
            statement.setString(3, playerName);
            statement.executeUpdate();

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            result = "Error occurred while updating database: " + ex.getMessage();
        }
        return result;
    }

    // Method to resize the image
    private Image resizeImage(String imagePath, int width, int height) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image image = icon.getImage();
        Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage).getImage();
    }

    public static void main(String[] args) {
        String playerName = JOptionPane.showInputDialog(null, "Enter your name:");
        SwingUtilities.invokeLater(() -> new RockPaperScissorsGUI(playerName));
    }
}
