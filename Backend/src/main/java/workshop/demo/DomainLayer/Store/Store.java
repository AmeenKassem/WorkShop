package workshop.demo.DomainLayer.Store;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DomainLayer.DiscountEntities.DiscountEntity;
import workshop.demo.DomainLayer.DiscountEntities.DiscountMapper;
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
    // in the db: 5 coulmns each is a counter
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
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "discount_id")
    private DiscountEntity discountEntity;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "policy_manager_id")
    private PolicyManager policyManager;

    public Store(String storeName, String cat) {
        this.storeName = storeName;
        this.category = cat;
        this.active = true;

    }

    public Store() {
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
            logger.error("Invalid rank {} forstore ", i);
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
        logger.debug("Ranked store= with rank={}", i);
        return true;
    }

    public int getFinalRateInStore() {
        logger.debug("Calculating final rank for store {}", storeId);

        int totalVotes = 0;
        int WRank = 0;
        int[] rank = {rank1, rank2, rank3, rank4, rank5};
        for (int i = 0; i < rank.length; i++) {
            int count = rank[i]; // votes for rank (i+1)
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
        if (discount == null && discountEntity != null) {
            DiscountEntity unproxied = (DiscountEntity) Hibernate.unproxy(discountEntity);
            discount = DiscountMapper.toDomain(unproxied);
        }
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
            MultiplyDiscount combo = new MultiplyDiscount(
                    "MULTIPLY(" + this.discount.getName() + "+" + d.getName() + ")");
            combo.addDiscount(discount);
            combo.addDiscount(d);
            this.discount = combo;

        }
    }

    public boolean removeDiscountByName(String name) {
        Discount root = getDiscount(); // reconstructs if null

        if (root == null) {
            return false;
        }
        if (root.getName().equals(name)) {
            this.discount = null;
            return true;
        }

        if (root instanceof CompositeDiscount composite) {
            boolean removed = composite.removeDiscountByName(name);
            if (removed && composite.getDiscounts().isEmpty()) {
                this.discount = null;
            }
            return removed;
        }

        return false;
    }

    public void addPurchasePolicy(PurchasePolicy p) {
        if (p == null) {
            throw new IllegalArgumentException("Purchase policy must not be null");
        }
        policyManager.addPolicy(p);
    }

    public boolean removePurchasePolicy(PurchasePolicy.PolicyType type, int productId, int param) {
        return policyManager.removePolicy(type, productId, param);
    }

    @Transactional
    public List<PurchasePolicy> getPurchasePolicies() {
        return List.copyOf(policyManager.getPurchasePolicies());
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

    public DiscountEntity getDiscountEntity() {
        if (discountEntity != null) {
            return (DiscountEntity) Hibernate.unproxy(discountEntity);
        }
        return null;
    }

    public void setDiscountEntity(DiscountEntity discountEntity) {
        this.discountEntity = discountEntity;
        if (discountEntity != null) {
            discountEntity.setStore(this);
            this.discount = DiscountMapper.toDomain(discountEntity); // 👈 sync right away
        } else {
            this.discount = null;
        }
    }

    public void assertPurchasePolicies(int age, int quantity, int productId) throws UIException {
        policyManager.assertPolicies(age, quantity, productId);
    }

    public void setPolicyManager(PolicyManager policyManager) {
        this.policyManager = policyManager;
    }

    public PolicyManager getPolicyManager() {
        return policyManager;
    }

    public List<String> getPurchasePoliciesStrings(int productId) {
        List<String> res = new ArrayList<>();
        for (PurchasePolicy string : getPurchasePolicies()) {
            if (string.getProductId() == productId) {
                res.add(string.violationMessage());
            }
        }
        return res;
    }
}
