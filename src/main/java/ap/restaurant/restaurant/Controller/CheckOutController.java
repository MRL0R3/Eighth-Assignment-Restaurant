package ap.restaurant.restaurant.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CheckOutController {

    @FXML private TableView<OrderItem> receiptTable;
    @FXML private TableColumn<OrderItem, String> colItem;
    @FXML private TableColumn<OrderItem, Integer> colQuantity;
    @FXML private TableColumn<OrderItem, Double> colUnitPrice;
    @FXML private TableColumn<OrderItem, Double> colTotal;

    @FXML private Label totalLabel;

    private final ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Mock data for now
        cartItems.addAll(
                new OrderItem("Pizza", 2, 12.99),
                new OrderItem("Burger", 1, 9.49)
        );

        colItem.setCellValueFactory(new PropertyValueFactory<>("name"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        receiptTable.setItems(cartItems);

        double total = cartItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    @FXML
    private void confirmOrder() {
        // Later: Save to DB
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Placed");
        alert.setHeaderText(null);
        alert.setContentText("Thanks for your purchase! 😊");
        alert.showAndWait();

        backToMenu();
    }

    @FXML
    private void backToMenu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ap/restaurant/restaurant/menu.fxml"));
            Stage stage = (Stage) receiptTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ap/restaurant/restaurant/login.fxml"));
            Stage stage = (Stage) receiptTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inner class to represent each row
    public static class OrderItem {
        private final String name;
        private final int quantity;
        private final double unitPrice;

        public OrderItem(String name, int quantity, double unitPrice) {
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotalPrice() { return unitPrice * quantity; }
    }
}
