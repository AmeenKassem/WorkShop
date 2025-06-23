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

    private int amount;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private double price;
    private int auctionId;
    private boolean isEnded;
    private boolean isTop;
    private int storeId;
    private int userId;
    private int productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'convertToDTO'");
    }

}
