package workshop.demo.DomainLayer.Stock;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

// import org.hibernate.engine.jdbc.env.internal.LobCreationLogging_.logger;
import org.springframework.beans.factory.annotation.Autowired;
// import org.hibernate.engine.jdbc.env.internal.LobCreationLogging_.logger;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import workshop.demo.ApplicationLayer.LockManager;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.AuctionStatus;
import workshop.demo.DTOs.SingleBidDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

@Entity
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int auctionId;// -->auction_id

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAuctionBid> bids = new ArrayList<>();

    private int productId;
    private int quantity;
    private AuctionStatus status;
    private double maxBid;
    private int winnerId = -1;
    private long endTimeMillis;

    @ManyToOne
    @JoinColumn(name = "active_store_id")
    private ActivePurcheses activePurcheses;

    public Auction(int productId, int quantity, long time, int id, int storeId, double min) {
        this.productId = productId;
        this.quantity = quantity;
        bids = new ArrayList<>();
        this.auctionId = id;
        this.status = AuctionStatus.IN_PROGRESS;
        this.endTimeMillis = System.currentTimeMillis() + time;
        maxBid = min;

    }

    public Auction() {
        status = AuctionStatus.IN_PROGRESS;
    }

    public void setActivePurchases(ActivePurcheses active) {
        activePurcheses = active;
    }

    @Transactional
    public void endAuction() {
        if (endTimeMillis > System.currentTimeMillis() || status == AuctionStatus.FINISH)
            return;
        for (UserAuctionBid UserAuctionBid : bids) {
            if (maxBid == UserAuctionBid.getBidPrice()) {
                winnerId = UserAuctionBid.getId();
                UserAuctionBid.markAsWinner();
            }
            UserAuctionBid.finishAuction();

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
    // this.storeId = storeId;
    // }

    public void setEndTimeMillis(long endTimeMillis) {
        this.endTimeMillis = endTimeMillis;
    }

    public boolean mustReturnToStock() {
        return status == AuctionStatus.FINISH && bids.isEmpty();
    }

    public UserAuctionBid bid(int userId, double price) throws UIException {

        if (status == AuctionStatus.FINISH || System.currentTimeMillis() > endTimeMillis) {
            throw new UIException("This auction has ended!", ErrorCodes.AUCTION_FINISHED);
        }
        if (price <= maxBid) {
            throw new UIException("Your bid must be higher than the current maximum bid!", ErrorCodes.BID_TOO_LOW);
        }
        for (UserAuctionBid userAuctionBid : bids) {
            if (userAuctionBid.getUserId() != userId)
                userAuctionBid.markAsLosedTop();
        }
        for (UserAuctionBid userAuctionBid : bids) {
            if (userAuctionBid.getUserId() == userId) {
                maxBid = price;
                userAuctionBid.setPrice(price);
                userAuctionBid.markAsCurrTop();
                return userAuctionBid;
            }
        }

        maxBid = price;
        UserAuctionBid bid = new UserAuctionBid();
        bid.setUserId(userId);
        bid.setPrice(price);
        bid.setAuction(this);
        bid.markAsCurrTop();
        bids.add(bid);
        return bid;

    }

    public AuctionDTO getDTO() {
        AuctionDTO res = new AuctionDTO();
        res.status = status;
        res.maxBid = maxBid;
        res.productId = productId;
        res.quantity = quantity;
        res.winnerId = winnerId;
        // if(winnerId!=-1){
        res.winnerUserId = -1;
        for (UserAuctionBid userAuctionBid : bids) {
            if (userAuctionBid.getId() == winnerId)
                res.winnerUserId = userAuctionBid.getUserId();
        }
        // }
        res.storeId = activePurcheses.getStoreId();
        res.endTimeMillis = this.endTimeMillis;
        res.endDate = getDateOfEnd();
        SingleBidDTO[] arrayBids = new SingleBidDTO[bids.size()];
        for (int i = 0; i < arrayBids.length; i++) {
            arrayBids[i] = bids.get(i).convertToDTO();
        }

        // TODO set a time date for ending product.
        res.auctionId = this.auctionId;
        res.bids = arrayBids; // this line wasnt here , so bids was always null
        return res;
    }

    public String getDateOfEnd() {
        // Convert to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(endTimeMillis),
                ZoneId.systemDefault() // Use your system time zone or ZoneId.of("UTC"), etc.
        );

        // Format to readable string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String formatted = dateTime.format(formatter);
        System.out.println("Formatted Time: " + formatted);
        return formatted;
    }

    public UserAuctionBid getWinner() {
        for (UserAuctionBid userAuctionBid : bids) {
            if (userAuctionBid.getId() == winnerId)
                return userAuctionBid;
        }
        return null;
    }

    @Transactional
    public boolean bidIsWinner(int bidId) {
        loadBids();
        return bidId == winnerId;
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

    public int getStoreId() {
        return activePurcheses.getStoreId();
    }

    public int getAmount() {
        return quantity;
    }

    public boolean isEnded() {
        return status == AuctionStatus.FINISH;
    }

    public boolean mustEnd() {
        return System.currentTimeMillis() < endTimeMillis;
    }

    public double getMaxBid() {
        return maxBid;
    }

    public boolean bidIsTop(int bidId) {
        for (UserAuctionBid userAuctionBid : bids) {
            if (userAuctionBid.getId() == bidId)
                return userAuctionBid.isCurrTop();

        }
        return false;
    }

    public long getRestMS() {
        return endTimeMillis - System.currentTimeMillis();
    }

    public int getTopId() {
        for (UserAuctionBid userAuctionBid : bids) {
            if (userAuctionBid.isCurrTop())
                return userAuctionBid.getUserId();
        }
        return -1;
    }

    public List<Integer> getBidsUsersIds() {
        List<Integer> res = new ArrayList<>();
        for (UserAuctionBid integer : bids) {
            res.add(integer.getUserId());
        }
        return res;
    }

    public void loadBids() {
        for (UserAuctionBid userAuctionBid : bids) {
            System.out.println("bid loaded");
        }
    }

    public int getWinnenId() {
        return winnerId;
    }

    public int getQuantity() {
        return quantity;
    }
}
