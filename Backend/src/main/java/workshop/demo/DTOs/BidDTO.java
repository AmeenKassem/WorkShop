package workshop.demo.DTOs;

public class BidDTO {
    public int productId;
    public int quantity;
    public boolean isAccepted;
    public int bidId;
    public SingleBid winner;//null if opened
    public SingleBid[] bids;
    public int storeId;
    public String storeName;
}
