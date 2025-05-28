package ap.restaurant.restaurant.Controller;


import ap.restaurant.restaurant.Entities.MenuItemData;
import ap.restaurant.restaurant.Service.SessionService;
import ap.restaurant.restaurant.Database.DatabaseManager;
import ap.restaurant.restaurant.Service.CartService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.sql.Connection;
import java.util.*;

public class OrderingController {
    @FXML
    private final Map<Integer, String> idToName = new LinkedHashMap<>();
    private final Map<String, Double> menu = new LinkedHashMap<>();

    @FXML
    private FlowPane menuPane;

    @FXML
    private ListView<String> cartListView;

    @FXML
    private Button checkoutButton;

    @FXML
    private Label welcomeLabel;

    @FXML
    private ComboBox<String> categoryBox;

    private List<MenuItemData> allItems = new ArrayList<>();


    @FXML
    public void initialize() {
        showUsername();

        try (Connection conn = DatabaseManager.getConnection()) {
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT id, name, price, image_url, category FROM menu_items");

            Set<String> categories = new TreeSet<>();
            allItems.clear();

            while (rs.next()) {
                allItems.add(new MenuItemData(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("image_url"),
                        rs.getString("category")
                ));
                categories.add(rs.getString("category"));
            }

            categoryBox.getItems().add("All");
            categoryBox.getItems().addAll(categories);
            categoryBox.setValue("All");

            categoryBox.setOnAction(e -> buildMenu(categoryBox.getValue()));
            buildMenu("All");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleCheckout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ap/restaurant/restaurant/checkout.fxml"));
            Stage stage = (Stage) cartListView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ap/restaurant/restaurant/login.fxml"));
            Stage stage = (Stage) cartListView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        SessionService.getInstance().logout(); // clear session

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Logged Out");
        alert.setHeaderText(null);
        alert.setContentText("You have been successfully logged out.");
        alert.showAndWait(); // Wait for user to close the dialog

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ap/restaurant/restaurant/login.fxml"));
            Stage stage = (Stage) cartListView.getScene().getWindow(); // or any other node
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void buildMenu(String categoryFilter) {
        menuPane.getChildren().clear();

        for (MenuItemData item : allItems) {
            if (!categoryFilter.equals("All") && !item.category.equalsIgnoreCase(categoryFilter)) {
                continue;
            }

            VBox card = new VBox(6);
            card.setStyle("-fx-background-color: #fff3; -fx-padding: 8; -fx-background-radius: 12;");
            card.setPrefWidth(140);
            card.setAlignment(Pos.CENTER);

            ImageView image = new ImageView(item.imageUrl);
            image.setFitHeight(100);
            image.setFitWidth(100);
            image.setPreserveRatio(true);

            Label nameLabel = new Label(item.name);
            Label priceLabel = new Label("$" + item.price);

            Button addBtn = new Button("Add");
            addBtn.setOnAction(e -> addToCart(item.name, item.price)); // ✅ item is valid here

            card.getChildren().addAll(image, nameLabel, priceLabel, addBtn);
            menuPane.getChildren().add(card);
        }
    }


    private void addToCart(String itemName, double price) {
        CartService.getInstance().addItem(itemName, price);
        refreshCartDisplay();
    }

    private void refreshCartDisplay() {
        cartListView.getItems().clear();

        for (var entry : CartService.getInstance().getCartItems().entrySet()) {
            String item = entry.getKey();
            int quantity = (int) entry.getValue()[0];
            cartListView.getItems().add(item + " x" + quantity);
        }
    }

    private void showUsername() {
        String username = SessionService.getInstance().getUsername();
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + username + " 👋");
        }
    }


}

