package workshop.demo.ApplicationLayer;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import workshop.demo.DataAccessLayer.UserJpaRepository;
import workshop.demo.DataAccessLayer.UserSuspensionJpaRepository;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.IStoreStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepoDB;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;

public class ActivePurchasesService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    @Autowired
    private UserJpaRepository userRepo;
    @Autowired
    private IStoreStockRepo storeStockRepo;
    @Autowired
    private UserSuspensionJpaRepository suspensionJpaRepo;
    @Autowired
    private SUConnectionRepository suConnectionRepo;
    @Autowired
    private IStoreRepoDB storeJpaRepo;
    @Autowired
    private NotificationService notifier;

    @Autowired
    private IAuthRepo authRepo;

    public int setProductToAuction(String token, int storeId, int productId, int quantity, long time, double startPrice)
            throws Exception, DevException {
        logger.info("Setting product {} to auction in store {}", productId, storeId);
        int userId = checkUserAndStore(token, storeId);
        //adding auction here:
            
        
        for (Node worker : suConnectionRepo.getOwnersInStore(storeId)) {
            String ownerName = userRepo.findById(worker.getMyId()).get().getUsername();
            notifier.sendDelayedMessageToUser(ownerName, "Owner "
                    + userRepo.findById(userId).get().getUsername() + " set a product to auction in your store");
        }
        // return stockRepo.addAuctionToStore(storeId, productId, quantity, time, startPrice);
        
        return -1;
    }

    private void checkUserRegisterOnline_ThrowException(int userId) throws UIException {
        Optional<Registered> user = userRepo.findById(userId);
        if (!user.isPresent())
            throw new UIException("stock service:user not found!", ErrorCodes.USER_NOT_FOUND);
    }

    private int checkUserAndStore(String token,int storeId) throws Exception{
        
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        // must add the exceptions here:
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> new UIException("store not found on db!", ErrorCodes.STORE_NOT_FOUND));
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to set produt to auction.", ErrorCodes.NO_PERMISSION);
        }
        return userId;
    }

}
