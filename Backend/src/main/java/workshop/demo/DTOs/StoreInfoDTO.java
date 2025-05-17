package workshop.demo.DTOs;

import java.util.List;

public class StoreInfoDTO {
    private String storeName;
    private List<ProductInfoDTO> products;

    public StoreInfoDTO(String storeName, List<ProductInfoDTO> products) {
        this.storeName = storeName;
        this.products = products;
    }
    public String getStoreName() { return storeName; }
    public List<ProductInfoDTO> getProducts() { return products; }
}
