
package workshop.demo.DomainLayer.Order;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;
    private int userId;
    private String date;
    @Transient
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

    public Order(int userId, ReceiptDTO receiptDTO, String storeName) {
        this.userId = userId;
        this.date = receiptDTO.getDate();
        this.productsList = receiptDTO.getProductsList();
        this.finalPrice = receiptDTO.getFinalPrice();
        this.storeName = storeName;
    }
    public Order() {
        // Default constructor for JPA
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

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public String getStoreName() {
        return storeName;
    }
}