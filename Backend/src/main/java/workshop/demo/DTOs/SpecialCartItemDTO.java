package workshop.demo.DTOs;

public class SpecialCartItemDTO {

    //x1
    public int storeId;
    public int specialId;
    public int bidId;
    public SpecialType type;
    //x2
    public String productName;
    public boolean isWinner;
    public boolean isEnded;

    public SpecialCartItemDTO() {
    }

    // Setter for x1 fields
    public void setIds(int storeId, int specialId, int bidId, SpecialType type) {
        this.storeId = storeId;
        this.specialId = specialId;
        this.bidId = bidId;
        this.type = type;
    }

    // Setter for x2 fields
    public void setValues(String productName, boolean isWinner, boolean isEnded) {
        this.productName = productName;
        this.isWinner = isWinner;
        this.isEnded = isEnded;
    }

    public int getStoreId() {
        return storeId;
    }

    public int getSpecialId() {
        return specialId;
    }

    public int getBidId() {
        return bidId;
    }

    public SpecialType getType() {
        return type;
    }

    public String getProductName() {
        return productName;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public boolean isEnded() {
        return isEnded;
    }
}
