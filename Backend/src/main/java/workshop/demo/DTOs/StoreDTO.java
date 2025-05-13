package workshop.demo.DTOs;

public class StoreDTO {
    private int storeId;
    private String storeName;
    private String category;
    private boolean active;
    private int finalRating;

    public StoreDTO(int storeId, String storeName, String category, boolean active, int finalRating) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.category = category;
        this.active = active;
        this.finalRating = finalRating;
    }
}
