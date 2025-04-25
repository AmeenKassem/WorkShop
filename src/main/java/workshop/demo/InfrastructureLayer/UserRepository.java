package workshop.demo.InfrastructureLayer;

import workshop.demo.DomainLayer.User.IUserRepo;

public class UserRepository implements IUserRepo {

    @Override
    public int getUserId(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserId'");
    }

    @Override
    public boolean isRegisterd(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isRegisterd'");
    }

    @Override
    public boolean isRegisterd(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isRegisterd'");
    }

    @Override
    public boolean isAdmin(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAdmin'");
    }

}
