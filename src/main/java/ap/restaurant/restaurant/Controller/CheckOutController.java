package ap.restaurant.restaurant.Controller;


import ap.restaurant.restaurant.Database.DatabaseManager;
import ap.restaurant.restaurant.Service.CartService;
import ap.restaurant.restaurant.Service.SessionService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

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
        Map<String, double[]> cart = CartService.getInstance().getCartItems();
        cartItems.clear();

        for (var entry : cart.entrySet()) {
            String name = entry.getKey();
            int qty = (int) entry.getValue()[0];
            double price = entry.getValue()[1];

            cartItems.add(new OrderItem(name, qty, price));
        }

        // Set up table columns (same as before)
        colItem.setCellValueFactory(new PropertyValueFactory<>("name"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        receiptTable.setItems(cartItems);

        double total = cartItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }


    private int getMenuItemIdByName(String name, Connection conn) throws Exception {
        String sql = "SELECT id FROM menu_items WHERE name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        } else {
            throw new Exception("Menu item not found: " + name);
        }
    }

    @FXML
    private void confirmOrder() {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction

            // STEP 1: Insert new order
            String insertOrder = "INSERT INTO orders (user_id, total_price) VALUES (?, ?) RETURNING id";
            PreparedStatement orderStmt = conn.prepareStatement(insertOrder);
            orderStmt.setInt(1, SessionService.getInstance().getUserId());

            orderStmt.setDouble(2, calculateTotal());
            ResultSet rs = orderStmt.executeQuery();
            rs.next();
            int orderId = rs.getInt(1);

            // STEP 2: Insert order details
            String insertDetail = "INSERT INTO order_details (order_id, menu_item_id, quantity, price) VALUES (?, ?, ?, ?)";
            PreparedStatement detailStmt = conn.prepareStatement(insertDetail);

            for (OrderItem item : cartItems) {
                int menuItemId = getMenuItemIdByName(item.getName(), conn); // helper below
                detailStmt.setInt(1, orderId);
                detailStmt.setInt(2, menuItemId);
                detailStmt.setInt(3, item.getQuantity());
                detailStmt.setDouble(4, item.getUnitPrice());
                detailStmt.addBatch();
            }

            detailStmt.executeBatch();
            conn.commit();

            // clear cart and show success
            CartService.getInstance().clearCart();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setContentText("Your order has been placed!");
            alert.showAndWait();
            backToMenu();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Could not complete the order.");
            alert.showAndWait();
        }
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

    private double calculateTotal() {
        return cartItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
    }

    private int getCurrentUserId() {
        return 1;
    }

    @FXML
    private void logout() {
        SessionService.getInstance().logout(); // clear session

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Logged Out");
        alert.setHeaderText(null);
        alert.setContentText("You have been successfully logged out.");
        alert.showAndWait(); // Wait for user to click OK

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ap/restaurant/restaurant/login.fxml"));
            Stage stage = (Stage) receiptTable.getScene().getWindow(); // ✅ valid UI node
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
