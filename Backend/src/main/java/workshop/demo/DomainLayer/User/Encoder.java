package workshop.demo.DomainLayer.User;

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


    public static void main (String[] args){
        Encoder e1= new Encoder();
        String pass1 = "$2a$10$yjaz7rxx3YQ68mi9vK4S6OycwTBY5ggWO3nqN7Q9IhJMMJPx0H4au";
        Encoder e2 = new Encoder(); 
        System.out.println(pass1);
        System.out.println(e2.matches(pass1, "123"));
        System.out.println(e2.matches( "123",pass1));
        

    }
}
