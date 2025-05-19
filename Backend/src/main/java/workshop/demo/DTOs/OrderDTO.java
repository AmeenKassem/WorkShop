package workshop.demo.DTOs;

import java.util.List;

public class OrderDTO {

    private int userId;
    private int storeId; //-> write get name by ID ->store 
    private String date;
    private List<ReceiptProduct> productsList;
    private double finalPrice;

    public OrderDTO(int userId, int storeId, String date, List<ReceiptProduct> productsList, double finalPrice) {
        this.userId = userId;
        this.storeId = storeId;
        this.date = date;
        this.productsList = productsList;
        this.finalPrice = finalPrice;
    }
    // ADDED GETTERS FOR TESTS
    

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<ReceiptProduct> getProductsList() {
        return productsList;
    }

    public void setProductsList(List<ReceiptProduct> productsList) {
        this.productsList = productsList;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }
}
