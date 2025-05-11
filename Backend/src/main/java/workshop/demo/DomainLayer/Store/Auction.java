<<<<<<< HEAD:Backend/src/main/java/workshop/demo/DomainLayer/Store/Auction.java
package workshop.demo.DomainLayer.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.AuctionStatus;
import workshop.demo.DTOs.SingleBid;
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

        SingleBid[] arrayBids = new SingleBid[bids.size()];
        for (int i = 0; i < arrayBids.length; i++)
            arrayBids[i] = bids.get(i);

        // TODO set a time date for ending product.
        res.auctionId = this.auctionId;
        return res;
    }
}
=======
package workshop.demo.DomainLayer.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.AuctionStatus;
import workshop.demo.DTOs.SingleBid;
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

        SingleBid[] arrayBids = new SingleBid[bids.size()];
        for (int i = 0; i < arrayBids.length; i++)
            arrayBids[i] = bids.get(i);

        // TODO set a time date for ending product.
        res.auctionId = this.auctionId;
        return res;
    }
}
>>>>>>> a5d20fe39422f9af1fbf9b58efcc0388f6605f11:src/main/java/workshop/demo/DomainLayer/Store/Auction.java
