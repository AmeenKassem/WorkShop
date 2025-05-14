package workshop.demo.DTOs;

public class StoreDTO {
    public int storeId;
    public String storeName;
    public String category;
    public boolean active;
    public int finalRating;

    public StoreDTO(int storeId, String storeName, String category, boolean active, int finalRating) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.category = category;
        this.active = active;
        this.finalRating = finalRating;
    }
}
