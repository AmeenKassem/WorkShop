package workshop.demo.DTOs;

import java.util.List;

public class PurchaseHistoryDTO {

    private String buyerUserName;
    private String storeName;
    private List<ReceiptProduct> items;
    private String timeStamp;
    private double totalPrice;

    public PurchaseHistoryDTO(String buyerUserName, String storeName, List<ReceiptProduct> items, String timeStamp, double totalPrice) {
        this.buyerUserName = buyerUserName;
        this.storeName = storeName;
        this.items = items;
        this.timeStamp = timeStamp;
        this.totalPrice = totalPrice;
    }

    public PurchaseHistoryDTO() {
    }

    public String getBuyerUserName() {
        return buyerUserName;
    }

    public void setBuyerUserName(String buyerUserName) {
        this.buyerUserName = buyerUserName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public List<ReceiptProduct> getItems() {
        return items;
    }

    public void setItems(List<ReceiptProduct> items) {
        this.items = items;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
