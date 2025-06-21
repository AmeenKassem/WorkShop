package workshop.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Purchase.IPaymentService;
import workshop.demo.DomainLayer.Purchase.IPurchaseRepo;
import workshop.demo.DomainLayer.Purchase.ISupplyService;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;

@Component
public class Repos {

    // public IUserSuspensionRepo UserSuspensionRepo = new UserSuspensionRepo();
    // public AdminInitilizer adminInitilizer = new AdminInitilizer("123321");
    // public IUserRepo userRepo = new UserRepository(new Encoder(), adminInitilizer);
    // public IStoreRepo storeRepo = new StoreRepository();
    // public AuthenticationRepo auth = new AuthenticationRepo();
    // @Autowired
    // public INotificationRepo notificationRepo;
    // public IOrderRepo orderRepo = new OrderRepository();
    // public IStockRepo stockrepo = new StockRepository();
    // public IPurchaseRepo purchaseRepo = new PurchaseRepository();
    // public IReviewRepo reviewRepo = new ReviewRepository();
    // public IPaymentService paymentService = new PaymentServiceImp();
    // public ISupplyService supplyService = new SupplyServiceImp();
    // public ISUConnectionRepo sUConnectionRepo = new SUConnectionRepository();
    // @Autowired
    // public IUserRepo userRepo;
    @Autowired
    public AdminInitilizer adminInitilizer;

    @Autowired
    public IStoreRepo storeRepo;

    @Autowired
    public AuthenticationRepo auth;

    @Autowired
    public INotificationRepo notificationRepo;

    @Autowired
    public IStockRepo stockrepo;

    @Autowired
    public IPurchaseRepo purchaseRepo;

    // @Autowired
    // public IReviewRepo reviewRepo;
    @Autowired
    public IPaymentService paymentService;

    @Autowired
    public ISupplyService supplyService;

    @Autowired
    public ISUConnectionRepo sUConnectionRepo;


}
