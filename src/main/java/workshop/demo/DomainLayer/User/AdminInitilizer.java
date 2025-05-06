package workshop.demo.DomainLayer.User;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.admin")
public class AdminInitilizer {
    private String password;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean matchPassword(String pass){
        if(password==null) throw new IllegalArgumentException("The system has no admin key!!");
        return pass==password;
    }

}
