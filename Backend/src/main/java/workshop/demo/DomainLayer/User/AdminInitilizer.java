package workshop.demo.DomainLayer.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
//@ConfigurationProperties(prefix = "app.admin")
public class AdminInitilizer {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitilizer.class);

    private String password = "123321";

    public AdminInitilizer(@Value("${app.admin.password}") String adminKey) {
        password = adminKey;
    }

    public String getPassword() {
        logger.debug("getPassword called");
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        logger.debug("Admin password initialized");
    }

    public boolean matchPassword(String pass) {
        logger.debug("matchPassword called");

        if (password == null) {
            logger.error("matchPassword failed: no admin password set in configuration");
            throw new IllegalArgumentException("The system has no admin key!!");
        }
        boolean match = password.equals(pass);
        logger.debug("Password match result: {}", match);
        return match;
    }
}
