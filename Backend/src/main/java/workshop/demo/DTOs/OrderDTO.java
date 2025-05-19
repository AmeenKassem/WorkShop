package workshop.demo.DTOs;

import java.util.List;

public class OrderDTO {

    private int userId;
    private int storeId; //-> write get name by ID ->store 
    private String date;
    private List<ReceiptProduct> productsList;
    private double finalPrice;

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

    // public String toFormattedString(String storeName) {
    //     StringBuilder sb = new StringBuilder();
    //     sb.append("🧾 Order from store: ").append(storeName).append("\n");
    //     sb.append("User ID: ").append(userId).append("\n");
    //     sb.append("Date: ").append(date).append("\n");
    //     sb.append("Final Price: ").append(finalPrice).append("\n");
    //     sb.append("Products:\n");
    //     for (ReceiptProduct p : productsList) {
    //         sb.append("- ").append(p.toString()).append("\n"); // customize if needed
    //     }
    //     return sb.toString();
    // }
}
