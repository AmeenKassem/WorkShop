package workshop.demo.DomainLayer.User;

public interface IUserRepo {

    public int getUserId(String token);

    public boolean isRegisterd(String token);

    

}
