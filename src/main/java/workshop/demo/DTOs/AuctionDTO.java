package workshop.demo.DTOs;

import java.util.List;
import java.util.Timer;

public class AuctionDTO {
    public SingleBid[] bids;
    public int storeId;
    public int productId;
    public AuctionStatus status;
    public double maxBid;
    // public <DateTime> timer;

    public SingleBid winner;
    public int quantity;

    

}
