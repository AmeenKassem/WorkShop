package workshop.demo.DomainLayer.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;

@Entity
public class Store {

    private static final Logger logger = LoggerFactory.getLogger(Store.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int storeId;
    @Column(unique = true)
    private String storeName;
    private String category;
    private boolean active;
    @Transient
    private AtomicInteger[] rank;// rank[x] is the number of people who ranked i+1
    //in the db: 5 coulmns each is a counter
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

    @Transient
    private Discount discount;
    @Transient
    private final List<PurchasePolicy> purchasePolicies = new ArrayList<>();

    public Store(String storeName, String cat) {
        this.storeName = storeName;
        this.category = cat;
        this.active = true;
        this.rank = new AtomicInteger[5];
        for (int i = 0; i < 5; i++) {
            rank[i] = new AtomicInteger(0);
        }
    }

    public Store() {
        this.rank = new AtomicInteger[5];
        for (int i = 0; i < 5; i++) {
            rank[i] = new AtomicInteger(0);
        }
    }

    @PostLoad
    private void initRankArray() {
        rank[0] = new AtomicInteger(rank1);
        rank[1] = new AtomicInteger(rank2);
        rank[2] = new AtomicInteger(rank3);
        rank[3] = new AtomicInteger(rank4);
        rank[4] = new AtomicInteger(rank5);
    }

    @PrePersist
    @PreUpdate
    private void updateRankColumns() {
        rank1 = rank[0].get();
        rank2 = rank[1].get();
        rank3 = rank[2].get();
        rank4 = rank[3].get();
        rank5 = rank[4].get();
    }

    public int getstoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public synchronized void setActive(boolean active) {
        logger.debug("Setting active status for store {} to {}", storeId, active);

        this.active = active;
    }

    // rank store:
    public boolean rankStore(int i) {
        if (i < 1 || i > 5) {
            logger.error("Invalid store rank: {}", i);

            return false;
        }
        rank[i - 1].incrementAndGet();
        switch (i) {
            case 1 ->
                rank1 = rank[0].get();
            case 2 ->
                rank2 = rank[1].get();
            case 3 ->
                rank3 = rank[2].get();
            case 4 ->
                rank4 = rank[3].get();
            case 5 ->
                rank5 = rank[4].get();
        }
        logger.debug("Ranking store {} with {}", storeId, i);

        return true;
    }

    public int getFinalRateInStore() {
        logger.debug("Calculating final rank for store {}", storeId);

        int totalVotes = 0;
        int WRank = 0;
        for (int i = 0; i < rank.length; i++) {
            int count = rank[i].get(); // votes for rank (i+1)
            totalVotes += count;
            WRank += (i + 1) * count;
        }
        if (totalVotes == 0) {
            return 3;// defult rank

        }
        int avgRank = (int) Math.round((double) WRank / totalVotes);
        return Math.max(1, Math.min(5, avgRank));// to make surre the result is between 1 and 5

    }

    public StoreDTO getStoreDTO() {
        return new StoreDTO(storeId, storeName, category, active, getFinalRateInStore());
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public void addDiscount(Discount d) {

        if (discount instanceof CompositeDiscount) {
            ((CompositeDiscount) discount).addDiscount(d);
        } else if (discount == null) {
            this.discount = d;
        } else {
            // wrap old and new discount into MaxDiscount by default
            MaxDiscount combo = new MaxDiscount("Auto-wrapped discounts");
            combo.addDiscount(discount);
            combo.addDiscount(d);
            this.discount = combo;

        }
    }

    public boolean removeDiscountByName(String name) {
        if (discount instanceof CompositeDiscount composite) {
            return composite.removeDiscountByName(name);
        } else if (discount != null && discount.getName().equals(name)) {
            this.discount = null;
            return true;
        }
        return false;
    }

    public void addPurchasePolicy(PurchasePolicy p) throws Exception {
        if (p == null) {
            throw new Exception("Policy must not be null");
        }
        purchasePolicies.add(p);
    }

    public void removePurchasePolicy(PurchasePolicy p) {
        purchasePolicies.remove(p);
    }

    public List<PurchasePolicy> getPurchasePolicies() {
        return Collections.unmodifiableList(purchasePolicies);
    }

    public void assertPurchasePolicies(UserDTO buyer, List<ItemStoreDTO> cart) throws Exception {
        for (PurchasePolicy p : purchasePolicies) {
            if (!p.isSatisfied(buyer, cart)) {
                throw new Exception(p.violationMessage());
            }
        }
    }

    public Discount findDiscountByName(String targetName) {
        if (discount == null || targetName == null) {
            return null;
        }
        return dfsFind(discount, targetName);
    }

    // ---------------- private helper ----------------
    private Discount dfsFind(Discount node, String targetName) {
        if (node.getName().equals(targetName)) {
            return node;
        }
        if (node instanceof CompositeDiscount comp) {
            for (Discount child : comp.getDiscounts()) {
                Discount found = dfsFind(child, targetName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null; // not found in this branch
    }

    public void setCategory(String category2) {
        category = category2;
    }

    public void setName(String storeName2) {
        this.storeName = storeName2;
    }

}
