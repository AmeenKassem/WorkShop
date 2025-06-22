package workshop.demo.DTOs;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import workshop.demo.DomainLayer.Order.Order;

@Entity
@Table(name = "receipt_product")
public class ReceiptProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String productName;
    @Enumerated(EnumType.STRING)
    private Category category;
    // private String description;
    private String storename;
    private int quantity;
    private int price;
    private int productId;
    private int storeId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public ReceiptProduct(String productName, String storename, int quantity, int price, int productId,
            Category category, int storeId) {
        this.productName = productName;
        this.storename = storename;
        this.quantity = quantity;
        this.price = price;
        this.productId = productId;
        this.category = category;
        this.storeId=storeId;
    }

    public ReceiptProduct() {
    }

    @Override
    public String toString() {
        return "ReceiptProduct{" +
                "productName='" + productName + '\'' +
                ", category=" + category +
                ", storename='" + storename + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", productId=" + productId +
                '}';
    }

    public String getProductName() {
        return productName;
    }

    public Category getCategory() {
        return category;
    }

    public String getStorename() {
        return storename;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setstoreName(String storeName) {
        this.storename = storeName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}
