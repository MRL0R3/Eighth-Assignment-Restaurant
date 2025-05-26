package ap.restaurant.restaurant.Cotroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class Contoller {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}