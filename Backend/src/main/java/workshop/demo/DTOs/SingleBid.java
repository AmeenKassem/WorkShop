package workshop.demo.DTOs;

public class SingleBid {

    private int amount;
    private int id;
    private double price;
    private SpecialType type;
    private int specialId;
    private Status status;
    private int storeId;
    private int userId;
    private int productId;

    public SingleBid(int productId, int amount, int userId, double price, SpecialType type, int storeId, int id,
            int specialId) {
        this.productId = productId;
        this.amount = amount;
        this.userId = userId;
        this.price = price;
        this.type = type;
        this.storeId = storeId;
        this.id = id;
        this.specialId = specialId;
        if (type == SpecialType.Auction) {
            status = Status.AUCTION_PENDING;
        } else {
            status = Status.BID_PENDING;
        }
    }

    public SingleBid() {
    }

    public double getBidPrice() {
        return this.price;
    }

    public void markAsWinner() {
        this.status = Status.AUCTION_WON;
    }

    public void markAsLosed() {
        this.status = Status.AUCTION_LOSED;
    }

    public int getId() {
        return this.id;
    }

    public void acceptBid() {
        this.status = Status.BID_ACCEPTED;
    }

    public void rejectBid() {
        this.status = Status.BID_REJECTED;
    }

    public int getUserId() {
        return this.userId;
    }

    public boolean isWon() {
        return this.status == Status.AUCTION_WON || this.status == Status.BID_ACCEPTED;
    }

    // Optional: add more getters if needed
    public Status getStatus() {
        return this.status;
    }

    public int getAmount() {
        return this.amount;
    }

    public SpecialType getType() {
        return this.type;
    }

    public int getSpecialId() {
        return this.specialId;
    }

    public int getStoreId() {
        return this.storeId;
    }

    public boolean isAccepted() {
        return this.status == Status.BID_ACCEPTED;
    }

    public boolean isWinner() {
        return this.status == Status.AUCTION_WON || this.status == Status.BID_ACCEPTED;
    }

    public boolean isEnded() {
        return status != Status.AUCTION_PENDING && status != Status.BID_PENDING;

    }

    public int productId() {
        return productId;

    }
}
