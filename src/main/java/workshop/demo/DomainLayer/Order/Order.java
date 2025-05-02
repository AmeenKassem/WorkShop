package workshop.demo.DomainLayer.Order;

import java.util.List;

import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;

public class Order {

    private int orderId;
    private int userId;
    private String date;
    private List<ReceiptProduct> productsList;
    private double finalPrice;
    private String storeName;

    public Order(int orderId, int userId, ReceiptDTO receiptDTO, String storeName) {
        this.orderId = orderId;
        this.userId = userId;
        this.productsList = receiptDTO.getProductsList();
        this.date = receiptDTO.getDate();
        this.finalPrice = receiptDTO.getFinalPrice();
        this.storeName = storeName;

    }

    public int getOrderId() {
        return orderId;
    }

    public int getUserId() {
        return userId;
    }

    public String getDate() {
        return date;
    }

    public List<ReceiptProduct> getProductsList() {
        return productsList;
    }

    public void setFinalPrice(int finalPrice) {
        this.finalPrice = finalPrice;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public String getStoreName() {
        return storeName;
    }
}
