package workshop.demo.DTOs;

public class ItemCartDTO {
    public int storeId;
    public Category category;
    public int productId;
    public int quantity;

    public ItemCartDTO(int storeId, Category category, int productId, int quantity) {
        this.storeId = storeId;
        this.category = category;
        this.productId = productId;
        this.quantity = quantity;
    }

    public int getProdutId() {
        return productId;
    }
}
