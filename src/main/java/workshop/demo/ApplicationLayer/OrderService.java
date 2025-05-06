package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.User.IUserRepo;
public class OrderService {

    private IOrderRepo orderRepo;
    private IStoreRepo storeRepo;
    private IAuthRepo authRepo;
    private IUserRepo userRepo;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderService(IOrderRepo orderRepo, IStoreRepo storeRepo, IAuthRepo authoRepo, IUserRepo userRepo) {
        this.orderRepo = orderRepo;
        this.storeRepo = storeRepo;
        this.authRepo = authoRepo;
        this.userRepo = userRepo;
        logger.info("created Order/history service");
    }

    public void setOrderToStore(int storeId, int userId, ReceiptDTO receiptDTO) throws Exception {
        logger.info("about to set an order to the history");
        Store store = this.storeRepo.findStoreByID(storeId);
        if (store == null) {
            logger.error("store not found!");
            throw new Exception("store not found");
        }
        this.orderRepo.setOrderToStore(storeId, userId, receiptDTO, store.getStoreName());
        logger.info("added the order to the history succeesfully!");

    }

    public List<OrderDTO> getAllOrderByStore(int storeId) throws Exception {
        logger.info("about to get all the orders that have been made in this history!");
        Store store = this.storeRepo.findStoreByID(storeId);
        if (store == null) {
            logger.error("store not found!");
            throw new Exception("store not found");
        }
        logger.info("about to get all the orders succsesfully!");
        return this.orderRepo.getAllOrderByStore(storeId);
    }

    public List<ReceiptDTO> getReceiptDTOsByUser(String token) throws Exception {
        logger.info("about to get all the recipts for the user!");
        if (!authRepo.validToken(token)) {
            throw new Exception("unvalid token!");
        }
        int userId = authRepo.getUserId(token);
        if (!userRepo.isRegistered(userId)) {
            throw new Exception(String.format("the user:%d is not registered to the system!", userId));
        }
        return this.orderRepo.getReceiptDTOsByUser(userId);
    }
}
