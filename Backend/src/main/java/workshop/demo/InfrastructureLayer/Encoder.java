package workshop.demo.InfrastructureLayer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


public class Encoder {
    private PasswordEncoder encoder =new BCryptPasswordEncoder();

    public String encodePassword(String password){
        return encoder.encode(password);
    }

    public boolean matches(String pass1,String pass2){
        return encoder.matches(pass1, pass2);
    }
}
