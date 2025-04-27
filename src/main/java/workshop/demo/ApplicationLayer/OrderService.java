package workshop.demo.ApplicationLayer;

import java.util.List;

import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Store;

public class OrderService {

    private IOrderRepo orderRepo;
    private IStoreRepo storeRepo;

    public OrderService(IOrderRepo orderRepo, IStoreRepo storeRepo) {
        this.orderRepo = orderRepo;
        this.storeRepo = storeRepo;
    }

    public void setOrderToStore(int storeId, int userId, ReceiptDTO receiptDTO) throws Exception {
        Store store = this.storeRepo.findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store not found");
        }
        this.orderRepo.setOrderToStore(storeId, userId, receiptDTO, store.getStoreName());

    }

    public List<OrderDTO> getAllOrderByStore(int storeId) throws Exception {
        Store store = this.storeRepo.findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store not found");
        }
        return this.orderRepo.getAllOrderByStore(storeId);
    }

    public List<ReceiptDTO> getReceiptDTOsByUser(int userId) throws Exception {

        return this.orderRepo.getReceiptDTOsByUser(userId);
    }
}
