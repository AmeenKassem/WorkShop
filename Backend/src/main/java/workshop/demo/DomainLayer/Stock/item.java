package workshop.demo.DomainLayer.Stock;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// import com.vaadin.flow.component.template.Id;

import jakarta.persistence.Transient;
import workshop.demo.DTOs.Category;

@Embeddable
public class item {
    private static final Logger logger = LoggerFactory.getLogger(item.class);

    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private int id;

    private int productId;
    private int quantity;
    private int price;
    private Category category;

    // @ManyToOne
    // @JoinColumn(name = "storeid")
    // private StoreStock store;

    @Transient
    private AtomicInteger[] rank;// rank[x] is the number of people who ranked i+1

    // discounts ...

    public item(){
        productId=1;
        rank=new AtomicInteger[5];
        for (int i = 0; i < 5; i++) {
            rank[i] = new AtomicInteger(0);
        }
    }

    public item(int produtId, int quantity, int price, Category category) {
        this.productId = produtId;
        this.price = price;
        this.quantity = quantity;
        
        this.rank = new AtomicInteger[5];
        for (int i = 0; i < 5; i++) {
            rank[i] = new AtomicInteger(0);
        }
        this.category = category;
    }

    public int getFinalRank() {
        logger.debug("Calculating final rank for productId={}", productId);
        int totalVotes = 0;
        int WRank = 0;
        for (int i = 0; i < rank.length; i++) {
            int count = rank[i].get(); // votes for rank (i+1)
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
        logger.debug("Ranking productId={} with rank={}", productId, i);

        rank[i - 1].incrementAndGet();
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

        this.quantity=(quantity);
    }

    public AtomicInteger[] getRank() {
        return rank;
    }

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
    public void setRank(AtomicInteger[] rank) {
        logger.debug("Overwriting rank array for productId={}", productId);

        this.rank = rank;
    }

    
}
