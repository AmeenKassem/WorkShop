package workshop.demo.DTOs;

import java.util.List;

public class ReceiptDTO {

    private String storeName; //-> write get name by ID ->store 
    private String date;
    private List<ReceiptProduct> productsList;
    private double finalPrice;

    public ReceiptDTO() {
        // Default constructor
    }
    
    public ReceiptDTO(String storeName, String date, List<ReceiptProduct> productsList, double finalPrice) {
        this.storeName = storeName;
        this.date = date;
        this.productsList = productsList;
        this.finalPrice = finalPrice;
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

}
