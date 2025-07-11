package workshop.demo.DTOs;

import java.util.List;

public class OrderDTO {

    private int userId;
    private int storeId; //-> write get name by ID ->store 
    private String date;
    private List<ReceiptProduct> productsList;
    private double finalPrice;

    public String userName;

    public OrderDTO() {
    }

    public OrderDTO(int userId, int storeId, String date, List<ReceiptProduct> productsList, double finalPrice) {
        this.userId = userId;
        this.storeId = storeId;
        this.date = date;
        this.productsList = productsList;
        this.finalPrice = finalPrice;
    }
    // ADDED GETTERS FOR TESTS

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setProductsList(List<ReceiptProduct> productsList) {
        this.productsList = productsList;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public int getUserId() {
        return userId;
    }

    public int getStoreId() {
        return storeId;
    }

    public String getDate() {
        return date;
    }

    public List<ReceiptProduct> getProductsList() {
        return productsList;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
