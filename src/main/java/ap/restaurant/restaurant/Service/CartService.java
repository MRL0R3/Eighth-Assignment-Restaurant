package ap.restaurant.restaurant.Service;

import java.util.LinkedHashMap;
import java.util.Map;

public class CartService {
    private static final CartService instance = new CartService();

    // itemName → [quantity, unitPrice]
    private final Map<String, double[]> cartMap = new LinkedHashMap<>();

    private CartService() {}

    public static CartService getInstance() {
        return instance;
    }

    public void addItem(String name, double price) {
        if (cartMap.containsKey(name)) {
            cartMap.get(name)[0]++; // increase quantity
        } else {
            cartMap.put(name, new double[]{1, price}); // new item
        }
    }

    public void clearCart() {
        cartMap.clear();
    }

    public Map<String, double[]> getCartItems() {
        return cartMap;
    }
}
