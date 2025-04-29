package workshop.demo.InfrastructureLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Order.Order;

public class OrderRepository implements IOrderRepo {

    private Map<Integer, List<Order>> history;//storeId
    //switch it when use database!!
    private static final AtomicInteger counterOId = new AtomicInteger(1);

    public static int generateId() {
        return counterOId.getAndIncrement();
    }

    public OrderRepository() {
        this.history = new HashMap<>();

    }

    @Override
    public void setOrderToStore(int storeId, int userId, ReceiptDTO receiptDTO, String storeName) {
        Order order = new Order(generateId(), userId, receiptDTO, storeName);
        if (!history.containsKey(storeId)) {
            history.put(storeId, new ArrayList<>());
        }
        history.get(storeId).add(order);

    }

    @Override
    public List<OrderDTO> getAllOrderByStore(int storeId) throws Exception {
        if (!history.containsKey(storeId)) {
            throw new Exception("store does not exist!");
        }
        List<OrderDTO> toReturn = new ArrayList<>();
        List<Order> tochange = this.history.get(storeId);
        for (Order order : tochange) {
            OrderDTO TOadd = new OrderDTO(order.getUserId(), storeId, order.getDate(), order.getProductsList(), order.getFinalPrice(),);
            toReturn.add(TOadd);
        }
        return toReturn;
    }

    @Override
    public List<ReceiptDTO> getReceiptDTOsByUser(int userId) throws Exception {
        boolean flag = false;
        List<ReceiptDTO> toReturn = new ArrayList<>();
        for (List<Order> orderList : history.values()) {
            for (Order order : orderList) {
                if (order.getUserId() == userId) {
                    flag = true;
                    ReceiptDTO receiptDTO = new ReceiptDTO(order.getStoreName(), order.getDate(), order.getProductsList(), order.getFinalPrice());
                    toReturn.add(receiptDTO);
                }
            }
        }
        if (!flag) {
            throw new Exception("user does not have receipts");
        }
        return toReturn;
    }

}
