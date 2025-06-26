package workshop.demo.DTOs;

import workshop.demo.DomainLayer.Stock.SingleBid;

public class AuctionDTO {

    public SingleBidDTO[] bids;
    public int storeId;
    public int productId;
    public AuctionStatus status;
    public double maxBid;
    // public <DateTime> timer;
    public int auctionId;
    public int winnerId;
    public String winnerUserName;
    public int quantity;
    public String storeName;
    public String productName;
    public long endTimeMillis;
    public String endDate;
    public int winnerUserId;
    public AuctionDTO() {
    }

}
