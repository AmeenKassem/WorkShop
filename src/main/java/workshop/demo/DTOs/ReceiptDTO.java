package workshop.demo.DTOs;

import java.util.List;

public class ReceiptDTO {

    private String storeName; //-> write get name by ID ->store 
    private String date;
    private List<ReceiptProduct> productsList;
    private int finalPrice;

    public ReceiptDTO(String storeName, String date, List<ReceiptProduct> productsList, int finalPrice) {
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

    public int getFinalPrice() {
        return finalPrice;
    }

}