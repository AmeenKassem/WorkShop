package workshop.demo.Contrrollers;

import org.springframework.stereotype.Component;

import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.InfrastructureLayer.AuthenticationService;
import workshop.demo.InfrastructureLayer.UserRepository;


@Component
public class Repos {
    public IUserRepo userRepo ;
    public IStoreRepo storeRepo ;
    public AuthenticationService auth=new AuthenticationService();


}
