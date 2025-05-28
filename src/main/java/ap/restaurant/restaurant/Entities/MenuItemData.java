package ap.restaurant.restaurant.Entities;

public class MenuItemData {
    public int id;
    public String name;
    public double price;
    public String imageUrl;
    public String category;

    public MenuItemData(int id, String name, double price, String imageUrl, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
    }
}
