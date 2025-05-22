package workshop.demo.DomainLayer.Order;

import java.util.List;

import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.ReceiptDTO;

public interface IOrderRepo {

    public void setOrderToStore(int storeId, int userId, ReceiptDTO receiptDTO, String storeName);

    public void addStoreTohistory(int storeId);

    public List<OrderDTO> getAllOrderByStore(int storeId) throws Exception;

    public List<ReceiptDTO> getReceiptDTOsByUser(int userId) throws Exception;

    public List<OrderDTO> getAllOrdersInSystem() throws Exception;

    public List<OrderDTO> getOrderDTOsByUserId(int userId) throws Exception;

}
