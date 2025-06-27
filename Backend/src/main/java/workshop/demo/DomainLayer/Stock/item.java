package workshop.demo.DomainLayer.Stock;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import workshop.demo.DTOs.Category;

@Embeddable
public class item {

    private static final Logger logger = LoggerFactory.getLogger(item.class);
    private int productId;
    private int quantity;
    private int price;
    private Category category;
    private int storeId;

    public void setStoreId(int id) {
        storeId = id;
    }
    @Column(name = "rank_1_count")
    private int rank1;
    @Column(name = "rank_2_count")
    private int rank2;
    @Column(name = "rank_3_count")
    private int rank3;
    @Column(name = "rank_4_count")
    private int rank4;
    @Column(name = "rank_5_count")
    private int rank5;

    // discounts ...
    public item() {
        productId = 1;
    }

    public item(int produtId, int quantity, int price, Category category) {
        this.productId = produtId;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public int getFinalRank() {
        logger.debug("Calculating final rank for productId={}", productId);
        int totalVotes = 0;
        int WRank = 0;
        // Correctly gather votes from actual rank fields
        int[] rank = {rank1, rank2, rank3, rank4, rank5};
        for (int i = 0; i < rank.length; i++) {
            int count = rank[i]; // votes for rank (i+1)
            totalVotes += count;
            WRank += (i + 1) * count;
        }
        if (totalVotes == 0) {
            logger.debug("No votes found for productId={}, returning default rank 3", productId);

            return 3;// defult rank

        }
        int avgRank = (int) Math.round((double) WRank / totalVotes);
        return Math.max(1, Math.min(5, avgRank));// to make surre the result is between 1 and 5
    }

    public boolean rankItem(int i) {
        if (i < 1 || i > 5) {
            logger.error("Invalid rank {} for productId={}", i, productId);
            return false;
        }
        switch (i) {
            case 1 ->
                rank1++;
            case 2 ->
                rank2++;
            case 3 ->
                rank3++;
            case 4 ->
                rank4++;
            case 5 ->
                rank5++;
        }
        logger.debug("Ranked productId={} with rank={}", productId, i);
        return true;
    }

    public int getQuantity() {
        return quantity;
    }

    public void AddQuantity() {
        logger.debug("Incrementing quantity for productId={}", productId);

        this.quantity++;
    }

    public void changeQuantity(int quantity) {
        logger.debug("Setting quantity for productId={} to {}", productId, quantity);

        this.quantity = (quantity);
    }

    public AtomicInteger[] getRank() {
        return new AtomicInteger[]{
            new AtomicInteger(rank1),
            new AtomicInteger(rank2),
            new AtomicInteger(rank3),
            new AtomicInteger(rank4),
            new AtomicInteger(rank5)
        };
    }

    public void setRank(AtomicInteger[] rank) {
        if (rank == null || rank.length != 5) {
            throw new IllegalArgumentException("Rank array must have exactly 5 elements");
        }
        this.rank1 = rank[0].get();
        this.rank2 = rank[1].get();
        this.rank3 = rank[2].get();
        this.rank4 = rank[3].get();
        this.rank5 = rank[4].get();

        logger.debug("Ranks set for productId={}: [{} {} {} {} {}]",
                rank1, rank2, rank3, rank4, rank5);
    }

    // public AtomicInteger[] getRank() {
    //     return rank;
    // }
    public int getProductId() {
        return productId;
    }

    public Category getCategory() {
        return category;
    }

    public synchronized int getPrice() {
        return price;
    }

    public synchronized void setPrice(int price) {
        logger.debug("Setting price for productId={} to {}", productId, price);

        this.price = price;
    }

    // for tests:
    // public void setRank(AtomicInteger[] rank) {
    //     logger.debug("Overwriting rank array for productId={}", productId);
    //     this.rank = rank;
    // }
    public int getStoreId() {
        return storeId;
    }

}
