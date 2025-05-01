package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Store;

public class OrderService {

    private IOrderRepo orderRepo;
    private IStoreRepo storeRepo;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderService(IOrderRepo orderRepo, IStoreRepo storeRepo) {
        this.orderRepo = orderRepo;
        this.storeRepo = storeRepo;
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

    public List<ReceiptDTO> getReceiptDTOsByUser(int userId) throws Exception {
        logger.info("about to get all the recipts for the user!");
        return this.orderRepo.getReceiptDTOsByUser(userId);
    }
}
