package workshop.demo.InfrastructureLayer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Encoder {

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encodePassword(String password) {
        return encoder.encode(password);
    }

    public boolean matches(String passInput, String encodedPass) {
        return encoder.matches(passInput, encodedPass);
    }


}
