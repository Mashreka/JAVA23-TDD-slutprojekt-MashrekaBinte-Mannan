import java.util.HashMap;
import java.util.Map;

public class Bank implements BankInterface {
    private Map<String, User> users = new HashMap<>();

    @Override
    public User getUserById(String id) {
        return users.get(id);
    }

    @Override
    public boolean isCardLocked(String id) {
        User user = users.get(id);
        return user != null && user.isLocked();
    }

    public static String getBankName() {
        return "RealBank";
    }
}
