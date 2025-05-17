package workshop.demo.DTOs;

public class ProductInfoDTO {
    private String productName;
    private int quantity;

    public ProductInfoDTO(String productName, int quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
}