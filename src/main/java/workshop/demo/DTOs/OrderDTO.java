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

}
