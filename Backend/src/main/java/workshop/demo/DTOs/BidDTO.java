package workshop.demo.DTOs;

import workshop.demo.DomainLayer.Stock.SingleBid;

public class BidDTO {

    public int productId;
    public int quantity;
    public boolean isAccepted;
    public int bidId;
    public SingleBid winner;//null if opened
    public SingleBidDTO[] bids;

    public BidDTO() {
    }
    public int storeId;
    public String storeName;
    public String productName;

    public SingleBidDTO[] getBids() {
        return bids;
    }
}
