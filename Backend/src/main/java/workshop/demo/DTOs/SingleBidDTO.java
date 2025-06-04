package workshop.demo.DTOs;

public class SingleBidDTO {

    public int id;
    public int productId;
    public int amount;
    public double price;
    public SpecialType type;
    public int specialId;
    public Status status;
    public int storeId;
    public int userId;
    public boolean isWinner;
    public boolean isAccepted;
    public boolean isEnded;

    public SingleBidDTO(int id, int productId, int amount, double price, SpecialType type, int specialId,
            Status status, int storeId, int userId,
            boolean isWinner, boolean isAccepted, boolean isEnded) {
        this.id = id;
        this.productId = productId;
        this.amount = amount;
        this.price = price;
        this.type = type;
        this.specialId = specialId;
        this.status = status;
        this.storeId = storeId;
        this.userId = userId;
        this.isWinner = isWinner;
        this.isAccepted = isAccepted;
        this.isEnded = isEnded;
    }

    public SingleBidDTO() {
    }
}
