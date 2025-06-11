package workshop.demo.DTOs;

public class ReceiptProduct {

    private String productName;
    private Category category;
    // private String description;
    private String storename;
    private int quantity;
    private int price;
    private int productId;

    public ReceiptProduct(String productName, String storename, int quantity, int price, int productId,
            Category category) {
        this.productName = productName;
        this.storename = storename;
        this.quantity = quantity;
        this.price = price;
        this.productId = productId;
        this.category = category;
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
}
