package workshop.demo.DomainLayer.Stock;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import workshop.demo.DTOs.SingleBidDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.Status;

@Entity
public class UserAuctionBid {

    // private int amount;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private double price;
    private boolean isEnded;
    private boolean isTop;
    private int userId;
    // private int storeId;
    // private int productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    // public void setAmount(int amount) {
    //     this.amount = amount;
    // }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    // public void setStoreId(int storeId) {
    //     this.storeId = storeId;
    // }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // public void setProductId(int productId) {
    //     this.productId = productId;
    // }

    public double getBidPrice() {
        return price;
    }

    public void markAsWinner() {
        isEnded = true;
    }

    public void finishAuction() {
        isEnded = true;
    }

    public boolean isWinner() {
        return isTop && isEnded;
    }

    public boolean isCurrTop(){
        return isTop;
    }

    public void markAsCurrTop() {
        isTop = true;
    }

    public void markAsLosedTop() {
        isTop = false;
    }

    public int getId() {
        return id;
    }

    public SingleBidDTO convertToDTO() {
        SingleBidDTO res = new SingleBidDTO();
        res.amount = auction.getAmount();
        res.productId = auction.getProductId();
        res.id = id;
        res.price= price;
        res.type = SpecialType.Auction;
        res.specialId = auction.getId();
        if(isWinner()) res.status = Status.AUCTION_WON;
        else if (isEnded) res.status = Status.AUCTION_LOSED;
        else res.status= Status.AUCTION_PENDING;
        res.storeId = auction.getStoreId();
        res.userId= userId;
        res.isWinner = isWinner();
        res.isEnded = isEnded;
        return res ;
    }

    public int getProductId() {
        return auction.getProductId();
    }

    public Auction getAuction() {
        return auction;
    }

    public Integer getStoreId() {
        return auction.getStoreId();
    }

    public int getAmount() {
        return auction.getAmount();
    }

    public int getUserId() {
        return userId;
    }

}
