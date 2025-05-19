package workshop.demo.DTOs;

public class ReceiptProduct {

    private String productName;
    private Category category;
    private String description;
    private String storename;
    private int quantity;
    private int price;

    public ReceiptProduct(String productName, Category category, String description, String storename, int quantity, int price) {
        this.productName = productName;
        this.category = category;
        this.description = description;
        this.storename = storename;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
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
    public void setstoreName(String storeName){
        this.storename = storeName;
    }
}
