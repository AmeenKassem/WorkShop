package workshop.demo.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.PurchaseHistoryDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Order.Order;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.InfrastructureLayer.IOrderRepoDB;
import workshop.demo.InfrastructureLayer.IStoreRepoDB;
import workshop.demo.InfrastructureLayer.UserJpaRepository;

@Service
public class OrderService {

    @Autowired
    private IOrderRepoDB orderJpaRepo;
    @Autowired
    private IAuthRepo authRepo;
    @Autowired
    private UserJpaRepository userRepo;
    @Autowired
    private IStoreRepoDB storeJpaRepo;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public List<OrderDTO> getAllOrderByStore(int storeId) throws Exception {
        logger.info("about to get all the orders that have been made in this history!");
        Optional<Store> store = storeJpaRepo.findById(storeId);
        if (!store.isPresent()) {
            logger.error("store not found!");
            throw new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND);
        }
        logger.info("about to get all the orders succsesfully!");
        String storeName = store.get().getStoreName();
        List<Order> orders = orderJpaRepo.findOrdersByStoreName(storeName);
        return convertToDTOs(orders, storeId);
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
        userRepo.findById(userId).orElseThrow(() -> new UIException(String.format("The user:%d is not registered to the system!", userId), ErrorCodes.USER_NOT_FOUND));
        List<Order> orders = orderJpaRepo.findOrdersByUserId(userId);
        List<ReceiptDTO> result = new ArrayList<>();
        for (Order order : orders) {
            result.add(new ReceiptDTO(order.getStoreName(), order.getDate(), order.getProductsList(), order.getFinalPrice()));
        }
        return result;
    }

    private List<OrderDTO> convertToDTOs(List<Order> orders, int storeId) {
        List<OrderDTO> dtos = new ArrayList<>();
        for (Order o : orders) {
            String userName = userRepo.findById(o.getUserId())
                    .map(registered -> registered.getUsername())
                    .orElse("Guest");

            //dtos.add(new OrderDTO(o.getUserId(), storeId, o.getDate(), o.getProductsList(), o.getFinalPrice()));
            OrderDTO dto = new OrderDTO(o.getUserId(), storeId, o.getDate(), o.getProductsList(), o.getFinalPrice());
            dto.setUserName(userName);

            dtos.add(dto);
        }
        return dtos;
    }


    public List<PurchaseHistoryDTO> viewPurchaseHistory(String token) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid Token!", ErrorCodes.INVALID_TOKEN);
        }

        List<Order> allOrders = orderJpaRepo.findAll();
        List<PurchaseHistoryDTO> historyEntries = new ArrayList<>();

        for (Order order : allOrders) {
            int buyerId = order.getUserId();
            String storeName = order.getStoreName();

            String buyerName = userRepo.findById(buyerId)
                    .map(u -> u.getUsername())
                    .orElse("Unknown buyer");

            List<ReceiptProduct> items = order.getProductsList();
            String timeStamp = order.getDate();
            double totalPrice = order.getFinalPrice();

            PurchaseHistoryDTO record = new PurchaseHistoryDTO(buyerName, storeName, items, timeStamp, totalPrice);
            historyEntries.add(record);
        }
        return historyEntries;
    }

}
