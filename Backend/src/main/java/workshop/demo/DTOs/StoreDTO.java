package workshop.demo.DTOs;

public class StoreDTO {

    public int storeId;
    public String storeName;
    public String category;
    public boolean active;
    public int finalRating;
    //  Required for Jackson to deserialize

    public StoreDTO() {
    }

    public StoreDTO(int storeId, String storeName, String category, boolean active, int finalRating) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.category = category;
        this.active = active;
        this.finalRating = finalRating;
    }

    public int getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public int getFinalRating() {
        return finalRating;
    }

}
