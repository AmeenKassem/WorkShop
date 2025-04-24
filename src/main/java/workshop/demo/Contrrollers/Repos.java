package workshop.demo.Contrrollers;

import org.springframework.stereotype.Component;

import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;

@Component
public class Repos {

    public IUserRepo userRepo;
    public IStoreRepo storeRepo;
    public AuthenticationRepo auth = new AuthenticationRepo();
    public INotificationRepo notificationRepo;

}
