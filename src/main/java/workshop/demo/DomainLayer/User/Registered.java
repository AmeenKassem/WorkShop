package workshop.demo.DomainLayer.User;

import workshop.demo.InfrastructureLayer.Encoder;

public class Registered extends Guest {

    private String username;
    private String encrybtedPassword; 

    public Registered(int id2,String username,String encrybtedPassword ) {
        super(id2);
        this.username=username;
        this.encrybtedPassword = encrybtedPassword;
    }

    public boolean check(Encoder encoder, String username, String password) {
        System.out.println("pass1 : "+password+",pass2:"+encrybtedPassword);
        return encoder.matches(password,encrybtedPassword) && username.equals(this.username);
    }

    
}
