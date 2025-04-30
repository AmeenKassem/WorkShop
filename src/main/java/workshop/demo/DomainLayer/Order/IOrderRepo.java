package workshop.demo.DomainLayer.Order;

import java.util.List;

import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.ReceiptDTO;

public interface IOrderRepo {

    public void setOrderToStore(int storeId, int userId, ReceiptDTO receiptDTO, String storeName);

    public List<OrderDTO> getAllOrderByStore(int storeId) throws Exception;

    public List<ReceiptDTO> getReceiptDTOsByUser(int userId) throws Exception;

}
