package workshop.demo.DTOs;

public class RandomDTO {

    public int productId;
    public int quantity;
    public ParticipationInRandomDTO[] participations;
    public int id;
    public int storeId;
    public String storeName;
    public ParticipationInRandomDTO winner;
    public double productPrice;
    public double amountLeft;
    public String productName;
    public long endTimeMillis;

    
    public String userName;
    public String endDate;
    
    public RandomDTO() {
    }
    public RandomDTO setStoreNameAndProductName(String productName, String storeName) {
        this.productName = productName;
        this.storeName = storeName;
        return this;
    }
    public int getProductId() {
        return productId;
    }
}
