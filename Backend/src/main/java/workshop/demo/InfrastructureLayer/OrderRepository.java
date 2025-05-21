package workshop.demo.InfrastructureLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Order.Order;

@Repository
public class OrderRepository implements IOrderRepo {

    private Map<Integer, List<Order>> history;
    private  final AtomicInteger counterOId = new AtomicInteger(1);

    public  int generateId() {
        return counterOId.getAndIncrement();
    }

    @Autowired
    public OrderRepository() {
        this.history = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void setOrderToStore(int storeId, int userId, ReceiptDTO receiptDTO, String storeName) {
        Order order = new Order(generateId(), userId, receiptDTO, storeName);
        history.computeIfAbsent(storeId, k -> new ArrayList<>()).add(order);
    }

    @Override
    public List<OrderDTO> getAllOrderByStore(int storeId) throws UIException {
        if (!history.containsKey(storeId)) {
            throw new UIException("Store does not exist!", ErrorCodes.STORE_NOT_FOUND);
        }

        List<OrderDTO> result = new ArrayList<>();
        for (Order order : history.get(storeId)) {
            result.add(new OrderDTO(order.getUserId(), storeId, order.getDate(), order.getProductsList(), order.getFinalPrice()));
        }
        return result;
    }

    @Override
    public List<ReceiptDTO> getReceiptDTOsByUser(int userId) throws UIException {
        boolean found = false;
        List<ReceiptDTO> result = new ArrayList<>();
        for (List<Order> orders : history.values()) {
            for (Order order : orders) {
                if (order.getUserId() == userId) {
                    found = true;
                    result.add(new ReceiptDTO(order.getStoreName(), order.getDate(), order.getProductsList(), order.getFinalPrice()));
                }
            }
        }
        if (!found) {
            throw new UIException("User has no receipts.", ErrorCodes.RECEIPT_NOT_FOUND);
        }
        return result;
    }

    @Override
    public List<OrderDTO> getAllOrdersInSystem() {
        List<OrderDTO> result = new ArrayList<>();
        for (var entry : history.entrySet()) {
            int storeId = entry.getKey();
            for (Order order : entry.getValue()) {
                result.add(new OrderDTO(order.getUserId(), storeId, order.getDate(), order.getProductsList(), order.getFinalPrice()));
            }
        }
        return result;
    }

    @Override
    public List<OrderDTO> getOrderDTOsByUserId(int userId) {
        List<OrderDTO> result = new ArrayList<>();
        for (var entry : history.entrySet()) {
            int storeId = entry.getKey();
            for (Order order : entry.getValue()) {
                if (order.getUserId() == userId) {
                    result.add(new OrderDTO(userId, storeId, order.getDate(), order.getProductsList(), order.getFinalPrice()));
                }
            }
        }
        return result;
    }
    public void clear() {
    if (history != null) {
        history.clear();
    }
    counterOId.set(1);
}
}
