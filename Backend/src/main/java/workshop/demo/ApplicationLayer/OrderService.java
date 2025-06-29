package workshop.demo.ApplicationLayer;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DataAccessLayer.UserJpaRepository;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.IStoreRepoDB;
import workshop.demo.DomainLayer.Store.Store;
// import workshop.demo.DomainLayer.User.IUserRepo;

@Service
public class OrderService {

    private IOrderRepo orderRepo;
    private IStoreRepo storeRepo;
    private IAuthRepo authRepo;
    private UserJpaRepository userRepo;
    private IStoreRepoDB storeJpaRepo;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    public OrderService(IOrderRepo orderRepo, IStoreRepo storeRepo, IAuthRepo authoRepo, UserJpaRepository userRepo,IStoreRepoDB storeJpaRepo) {
        this.orderRepo = orderRepo;
        this.storeRepo = storeRepo;
        this.authRepo = authoRepo;
        this.userRepo = userRepo;
        this.storeJpaRepo = storeJpaRepo;
        logger.info("created Order/history service");
    }



    public List<OrderDTO> getAllOrderByStore(int storeId) throws Exception {
        logger.info("about to get all the orders that have been made in this history!");
        Optional<Store> store = storeJpaRepo.findById(storeId);
        if (!store.isPresent()) {
            logger.error("store not found!");
            throw new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND);
        }
        logger.info("about to get all the orders succsesfully!");
        return this.orderRepo.getAllOrderByStore(storeId);
    }

    public List<ReceiptDTO> getReceiptDTOsByUser(String token) throws Exception {
        logger.info("about to get all the recipts for the user!");
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        // if (!userRepo.isRegistered(userId)) {
        //     throw new UIException(String.format("The user:%d is not registered to the system!", userId), ErrorCodes.USER_NOT_FOUND);
        // }
        userRepo.findById(userId).orElseThrow(()->new UIException(String.format("The user:%d is not registered to the system!", userId), ErrorCodes.USER_NOT_FOUND));
        return this.orderRepo.getReceiptDTOsByUser(userId);
    }
}
