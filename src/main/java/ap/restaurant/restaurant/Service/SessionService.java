package ap.restaurant.restaurant.Service;

public class SessionService {
    private static SessionService instance;
    private int userId;
    private String username;

    private SessionService() {}

    public static SessionService getInstance() {
        if (instance == null) {
            instance = new SessionService();
        }
        return instance;
    }

    public void login(int id, String username) {
        this.userId = id;
        this.username = username;
    }

    public void logout() {
        this.userId = -1;
        this.username = null;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public boolean isLoggedIn() {
        return userId > 0;
    }
}
