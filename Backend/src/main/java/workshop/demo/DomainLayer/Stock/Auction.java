package workshop.demo.DomainLayer.Stock;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.security.access.method.P;

import com.github.javaparser.ast.Generated;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.AuctionStatus;
import workshop.demo.DTOs.SingleBidDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

@Entity
public class Auction {

    private int productId;

    private int quantity;
    private AuctionStatus status;
    @Transient
    private Timer timer;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int auctionId;//-->auction_id

    // @OneToOne(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval =
    // true, fetch = FetchType.LAZY)
    @Transient
    private List<UserAuctionBid> bids;
    private double maxBid;
    @Transient
    private UserAuctionBid winner;
    // private int storeId;
    @Transient
    private AtomicInteger idGen = new AtomicInteger();
    @Transient
    private final Object lock = new Object();
    private long endTimeMillis;

    @ManyToOne
    @JoinColumn(name = "active_store_id")
    private ActivePurcheses activePurcheses;

    public Auction(int productId, int quantity, long time, int id, int storeId, double min) {
        this.productId = productId;
        this.quantity = quantity;
        this.timer = new Timer();
        bids = new ArrayList<>();
        // this.storeId = storeId;
        this.auctionId = id;
        this.status = AuctionStatus.IN_PROGRESS;
        this.endTimeMillis = System.currentTimeMillis() + time;
        maxBid = min;
        // timer.schedule(new TimerTask() {
        // @Override
        // public void run() {
        // for (UserAuctionBid UserAuctionBid : bids) {
        // if (maxBid == UserAuctionBid.getBidPrice()) {
        // winner = UserAuctionBid;
        // winner.markAsWinner();
        // } else {
        // UserAuctionBid.markAsLosed();
        // }
        // }
        // status = AuctionStatus.FINISH;
        // }
        // }, time);
    }

    public Auction() {
        
    }

    public void setActivePurchases(ActivePurcheses active){
        activePurcheses=active;
    }

    public void endAuction() {
        for (UserAuctionBid UserAuctionBid : bids) {
            if (maxBid == UserAuctionBid.getBidPrice()) {
                winner = UserAuctionBid;
                winner.finishAuction();
            } else {
                UserAuctionBid.finishAuction();
            }
        }
        status = AuctionStatus.FINISH;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setMaxBid(double maxBid) {
        this.maxBid = maxBid;
    }

    // public void setStoreId(int storeId) {
    //     this.storeId = storeId;
    // }

    public void setEndTimeMillis(long endTimeMillis) {
        this.endTimeMillis = endTimeMillis;
    }

    public boolean mustReturnToStock() {
        return status == AuctionStatus.FINISH && bids.isEmpty();
    }

    public UserAuctionBid bid(int userId, double price) throws UIException {
        synchronized (lock) {
            if (status == AuctionStatus.FINISH) {
                throw new UIException("This auction has ended!", ErrorCodes.AUCTION_FINISHED);
            }
            if (price <= maxBid) {
                throw new UIException("Your bid must be higher than the current maximum bid!", ErrorCodes.BID_TOO_LOW);
            }

            maxBid = price;

            // UserAuctionBid bid = new UserAuctionBid(productId, quantity, userId, price,
            // SpecialType.Auction, storeId,
            // idGen.incrementAndGet(), auctionId);

            // bids.add(bid);

            return null;
        }
    }

    public AuctionDTO getDTO() {
        AuctionDTO res = new AuctionDTO();
        res.status = status;
        res.maxBid = maxBid;
        res.productId = productId;
        res.quantity = quantity;
        res.winner = winner.convertToDTO();
        // res.storeId = storeId;
        res.endTimeMillis = this.endTimeMillis;

        SingleBidDTO[] arrayBids = new SingleBidDTO[bids.size()];
        for (int i = 0; i < arrayBids.length; i++) {
            arrayBids[i] = bids.get(i).convertToDTO();
        }

        // TODO set a time date for ending product.
        res.auctionId = this.auctionId;
        res.bids = arrayBids; // this line wasnt here , so bids was always null
        return res;
    }

    public UserAuctionBid getWinner() {
        return winner;
    }

    public boolean bidIsWinner(int bidId) {
        return winner != null && winner.getId() == bidId;
    }

    public UserAuctionBid getBid(int bidId) {
        for (UserAuctionBid UserAuctionBid : bids) {
            if (UserAuctionBid.getId() == bidId) {
                return UserAuctionBid;
            }
        }
        return null;
    }

    public int getProductId() {
        return productId;
    }

    public Integer getId() {
        return auctionId;
    }
}
