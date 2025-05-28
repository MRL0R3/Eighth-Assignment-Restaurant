package ap.restaurant.restaurant.Controller;

import ap.restaurant.restaurant.Database.DatabaseManager;
import ap.restaurant.restaurant.Service.SessionService;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Duration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class LoginSignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    // Fake user data for demo (replace with DB logic later)
    private final String DEMO_USERNAME = "admin";
    private final String DEMO_PASSWORD = "1234"; // You can hash this if needed
    @FXML
    private TextField newUsernameField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label signupErrorLabel;

    @FXML
    private Label strengthLabel;

    @FXML
    public void initialize() {
        newPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            strengthLabel.setText(evaluatePasswordStrength(newText));
            strengthLabel.setStyle("-fx-text-fill: " + getStrengthColor(newText));
        });
    }


    @FXML
    private void handleSignup(ActionEvent event) {
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            signupErrorLabel.setText("Please fill all fields.");
            signupErrorLabel.setVisible(true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            signupErrorLabel.setText("Passwords do not match.");
            signupErrorLabel.setVisible(true);
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            // Check if username already exists
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                signupErrorLabel.setText("Username is already taken.");
                signupErrorLabel.setVisible(true);
                return;
            }

            // Store hashed password
            String hashed = hashPassword(password);
            String insertSql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, username);
            insertStmt.setString(2, hashed);
            insertStmt.executeUpdate();

            signupErrorLabel.setStyle("-fx-text-fill: green;");
            signupErrorLabel.setText("Account created! Redirecting...");
            signupErrorLabel.setVisible(true);

            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(e -> goToLogin(null));
            delay.play();

        } catch (Exception e) {
            e.printStackTrace();
            signupErrorLabel.setText("Error connecting to database.");
            signupErrorLabel.setVisible(true);
        }
    }


    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ap/restaurant/restaurant/login.fxml"));
            Stage stage = (Stage) newUsernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            errorLabel.setVisible(true);
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT id, password_hash FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                int userId = rs.getInt("id");

                if (storedHash.equals(hashPassword(password))) {
                    // ✅ Login successful
                    SessionService.getInstance().login(userId, username);
                    loadMenuScene(); // switch to menu.fxml
                } else {
                    errorLabel.setText("Incorrect password.");
                    errorLabel.setVisible(true);
                }
            } else {
                errorLabel.setText("Username not found.");
                errorLabel.setVisible(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error connecting to database.");
            errorLabel.setVisible(true);
        }
    }


    @FXML
    private void goToSignup(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ap/restaurant/restaurant/signup.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String evaluatePasswordStrength(String password) {
        if (password.length() < 6) return "Too short";
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[!@#$%^&*()].*");

        int score = 0;
        if (hasUpper) score++;
        if (hasLower) score++;
        if (hasDigit) score++;
        if (hasSymbol) score++;

        return switch (score) {
            case 0, 1 -> "Weak";
            case 2 -> "Fair";
            case 3 -> "Good";
            case 4 -> "Strong 💪";
            default -> "";
        };
    }

    private String getStrengthColor(String password) {
        int length = password.length();
        int score = (length >= 6 ? 1 : 0)
                + (password.matches(".*[A-Z].*") ? 1 : 0)
                + (password.matches(".*\\d.*") ? 1 : 0)
                + (password.matches(".*[!@#$%^&*()].*") ? 1 : 0);

        return switch (score) {
            case 0, 1 -> "red";
            case 2 -> "orange";
            case 3 -> "blue";
            case 4 -> "green";
            default -> "gray";
        };
    }




    private void loadMenuScene() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ap/restaurant/restaurant/menu.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
