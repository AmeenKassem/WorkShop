package workshop.demo.DTOs;

import java.util.List;

public class ReceiptDTO {

    private String storeName; //-> write get name by ID ->store 
    private String date;
    private List<ReceiptProduct> productsList;
    private double finalPrice;
    private int paymentTransactionId;
    private int supplyTransactionId;

    public ReceiptDTO() {
        // Default constructor
    }

    public ReceiptDTO(String storeName, String date, List<ReceiptProduct> productsList, double finalPrice) {
        this.storeName = storeName;
        this.date = date;
        this.productsList = productsList;
        this.finalPrice = finalPrice;
    }
    public ReceiptDTO(String storeName, String date, List<ReceiptProduct> productsList, double finalPrice,int paymentTransactionId
    ,int supplyTransactionId) {
        this.storeName = storeName;
        this.date = date;
        this.productsList = productsList;
        this.finalPrice = finalPrice;
        this.paymentTransactionId=paymentTransactionId;
        this.supplyTransactionId=supplyTransactionId;
    }

    public String getStoreName() {
        return storeName;
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

    public void setStoreName(String storeName) {
        this.storeName = storeName;
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
    public int getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(int paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public int getSupplyTransactionId() {
        return supplyTransactionId;
    }

    public void setSupplyTransactionId(int supplyTransactionId) {
        this.supplyTransactionId = supplyTransactionId;
    }


}
