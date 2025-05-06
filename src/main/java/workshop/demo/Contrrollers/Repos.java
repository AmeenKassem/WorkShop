package workshop.demo.Contrrollers;

import org.springframework.stereotype.Component;

import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Purchase.IPurchaseRepo;
import workshop.demo.DomainLayer.Review.IReviewRepo;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;

@Component
public class Repos {

    public IUserRepo userRepo;
    public IStoreRepo storeRepo;
    public AuthenticationRepo auth = new AuthenticationRepo();
    public INotificationRepo notificationRepo;
    public IOrderRepo orderRepo;
    public IStockRepo stockrepo;
    public IPurchaseRepo purchaseRepo;
    public IReviewRepo reviewRepo;
    // public ProductFilter productFilter;

}
