package workshop.demo.DomainLayer.Exceptions;

public class IncorrectLogin extends RuntimeException{

    public IncorrectLogin(){
        super("Incorrect password or username!");
    }
}
