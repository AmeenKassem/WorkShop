package workshop.demo.DomainLayer.Stock;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.security.access.method.P;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.AuctionStatus;
import workshop.demo.DTOs.SingleBidDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

public class Auction {

    private int productId;
    private int quantity;
    private AuctionStatus status;
    private Timer timer;
    private int auctionId;
    private List<SingleBid> bids;
    private double maxBid;
    private SingleBid winner;
    private int storeId;
    private AtomicInteger idGen = new AtomicInteger();
    private final Object lock = new Object();

    public Auction(int productId, int quantity, long time, int id, int storeId) {
        this.productId = productId;
        this.quantity = quantity;
        this.timer = new Timer();
        bids = new ArrayList<>();
        this.storeId = storeId;
        this.auctionId = id;
        this.status = AuctionStatus.IN_PROGRESS;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (SingleBid singleBid : bids) {
                    if (maxBid == singleBid.getBidPrice()) {
                        winner = singleBid;
                        winner.markAsWinner();
                    } else {
                        singleBid.markAsLosed();
                    }
                }
                status = AuctionStatus.FINISH;
            }
        }, time);
    }

    public void endAuction() {
        for (SingleBid singleBid : bids) {
            if (maxBid == singleBid.getBidPrice()) {
                winner = singleBid;
                winner.markAsWinner();
            } else {
                singleBid.markAsLosed();
            }
        }
        status = AuctionStatus.FINISH;
    }

    public boolean mustReturnToStock(){
        return status==AuctionStatus.FINISH && bids.isEmpty();
    }

    
    public SingleBid bid(int userId, double price) throws UIException {
        synchronized (lock) {
            if (status == AuctionStatus.FINISH) {
                throw new UIException("This auction has ended!", ErrorCodes.AUCTION_FINISHED);
            }
            if (price <= maxBid) {
                throw new UIException("Your bid must be higher than the current maximum bid!", ErrorCodes.BID_TOO_LOW);
            }

            maxBid = price;

            SingleBid bid = new SingleBid(productId, quantity, userId, price, SpecialType.Auction, storeId,
                    idGen.incrementAndGet(), auctionId);

            bids.add(bid);

            return bid;
        }
    }

    public AuctionDTO getDTO() {
        AuctionDTO res = new AuctionDTO();
        res.status = status;
        res.maxBid = maxBid;
        res.productId = productId;
        res.quantity = quantity;
        res.winner = winner;
        res.storeId = storeId;

        SingleBidDTO[] arrayBids = new SingleBidDTO[bids.size()];
        for (int i = 0; i < arrayBids.length; i++) {
            arrayBids[i] = bids.get(i).convertToDTO();
        }

        // TODO set a time date for ending product.
        res.auctionId = this.auctionId;
        res.bids = arrayBids; // this line wasnt here , so bids was always null
        return res;
    }

    public SingleBid getWinner() {
        return winner;
    }

    public boolean bidIsWinner(int bidId) {
        return winner != null && winner.getId() == bidId;
    }

    public SingleBid getBid(int bidId) {
        for (SingleBid singleBid : bids) {
            if (singleBid.getId() == bidId) {
                return singleBid;
            }
        }
        return null;
    }

    public int getProductId() {
        return productId;
    }
}
