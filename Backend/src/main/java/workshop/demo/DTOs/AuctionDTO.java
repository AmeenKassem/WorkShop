package workshop.demo.DTOs;

public class AuctionDTO {

    public SingleBid[] bids;
    public int storeId;
    public int productId;
    public AuctionStatus status;
    public double maxBid;
    // public <DateTime> timer;
    public int auctionId;
    public SingleBid winner;
    public int quantity;
    public String storeName;

    public AuctionDTO() {
    }

}
