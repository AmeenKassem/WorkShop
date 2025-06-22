package workshop.demo.ApplicationLayer;

import java.util.ArrayList;
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
import workshop.demo.DomainLayer.Order.IOrderRepoDB;
import workshop.demo.DomainLayer.Order.Order;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.IStoreRepoDB;
import workshop.demo.DomainLayer.Store.Store;
// import workshop.demo.DomainLayer.User.IUserRepo;

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
            dtos.add(new OrderDTO(o.getUserId(), storeId, o.getDate(), o.getProductsList(), o.getFinalPrice()));
        }
        return dtos;
    }

}
