package workshop.demo.DomainLayer.Exceptions;

public class GuestNotFoundException extends RuntimeException{
    public GuestNotFoundException(int id){
        super("user guest not found,id: "+id);
    }
}
