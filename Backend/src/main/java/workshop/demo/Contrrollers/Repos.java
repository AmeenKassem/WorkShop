package workshop.demo.Contrrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.SupplyServiceImp;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Purchase.IPaymentService;
import workshop.demo.DomainLayer.Purchase.IPurchaseRepo;
import workshop.demo.DomainLayer.Purchase.ISupplyService;
import workshop.demo.DomainLayer.Review.IReviewRepo;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.NotificationRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;
import workshop.demo.InfrastructureLayer.PurchaseRepository;
import workshop.demo.InfrastructureLayer.ReviewRepository;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.UserRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionRepo;

@Component
public class Repos {

    public  IUserSuspensionRepo UserSuspensionRepo = new UserSuspensionRepo();
    public AdminInitilizer adminInitilizer = new AdminInitilizer("123321");
    public IUserRepo userRepo = new UserRepository(new Encoder(), adminInitilizer);
    public IStoreRepo storeRepo=new StoreRepository();
    public AuthenticationRepo auth = new AuthenticationRepo();
    @Autowired public INotificationRepo notificationRepo;
    public IOrderRepo orderRepo= new OrderRepository();
    public IStockRepo stockrepo=new StockRepository();
    public IPurchaseRepo purchaseRepo= new PurchaseRepository();
    public IReviewRepo reviewRepo= new ReviewRepository();
    public IPaymentService paymentService= new PaymentServiceImp();
    public ISupplyService supplyService=new SupplyServiceImp();
    public ISUConnectionRepo sUConnectionRepo=new SUConnectionRepository();
    // public ProductFilter productFilter;
    // public IUserSuspensionRepo userRusepo;
    // public IUserSuspensionRepo userSusRepo=new UserSuspensionRepo();

}
