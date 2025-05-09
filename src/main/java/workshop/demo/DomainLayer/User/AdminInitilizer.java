package workshop.demo.DomainLayer.User;

public class AdminInitilizer {

    private final String password;

    public AdminInitilizer(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Admin password cannot be null or blank.");
        }
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public boolean matchPassword(String pass) {
        if (password == null) {
            throw new IllegalArgumentException("The system has no admin key!!");
        }
        return password.equals(pass);
    }
}
