package workshop.demo.DTOs;

import workshop.demo.DomainLayer.Stock.SingleBid;

public class AuctionDTO {

    public SingleBidDTO[] bids;
    public int storeId;
    public int productId;
    public AuctionStatus status;
    public double maxBid;
    public double startPrice;
    // public <DateTime> timer;
    public int auctionId;
    public SingleBid winner;
    public int quantity;
    public String storeName;
    public String productName;

    public AuctionDTO() {
    }

}
