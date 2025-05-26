package ap.restaurant.restaurant.Controller;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Duration;

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

        if (username.equals(DEMO_USERNAME)) {
            signupErrorLabel.setText("Username already taken.");
            signupErrorLabel.setVisible(true);
            return;
        }

        // Simulate account creation (later you can insert into DB)
        signupErrorLabel.setText("Account created! Redirecting...");
        signupErrorLabel.setStyle("-fx-text-fill: green;");
        signupErrorLabel.setVisible(true);

        // Simulate delay before redirecting to login
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(e -> goToLogin(null));
        delay.play();

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

        // Simple credential check (can later be DB check)
        if (username.equals(DEMO_USERNAME) && password.equals(DEMO_PASSWORD)) {
            loadMenuScene();  // Navigate to menu.fxml
        } else {
            errorLabel.setText("Invalid username or password.");
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
