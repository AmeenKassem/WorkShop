package workshop.demo.DomainLayer.Store;

import jakarta.persistence.*;

@Entity
@Table(name = "purchase_policy")
public class PurchasePolicy implements StorePolicy {

    public enum PolicyType {
        NO_PRODUCT_UNDER_AGE,
        MIN_QTY_PER_PRODUCT,
        SALE_POLICY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_manager_id")
    private PolicyManager policyManager;

    @Column(name = "product_id", nullable = false)
    private int productId;

    // param is minAge for NO_PRODUCT_UNDER_AGE, minQty for MIN_QTY_PER_PRODUCT
    @Column(name = "param", nullable = false)
    private int param;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false)
    private PolicyType policyType;

    public PurchasePolicy() {
        // JPA no-arg constructor
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public PolicyManager getPolicyManager() {
        return policyManager;
    }

    public void setPolicyManager(PolicyManager policyManager) {
        this.policyManager = policyManager;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    public PolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }

    // Implement StorePolicy interface by delegating to static factory methods
    @Override
    public boolean isSatisfied(int age, int quantity, int productId) {
        switch (policyType) {
            case NO_PRODUCT_UNDER_AGE:
                return noProductUnderAge(this.productId, this.param).isSatisfied(age, quantity, productId);
            case MIN_QTY_PER_PRODUCT:
                return minQuantityPerProduct(this.productId, this.param).isSatisfied(age, quantity, productId);
            default:
                throw new IllegalStateException("Unknown policy type: " + policyType);
        }
    }

    @Override
    public String violationMessage() {
        switch (policyType) {
            case NO_PRODUCT_UNDER_AGE:
                return noProductUnderAge(this.productId, this.param).violationMessage();
            case MIN_QTY_PER_PRODUCT:
                return minQuantityPerProduct(this.productId, this.param).violationMessage();
            default:
                return "Unknown policy violation.";
        }
    }

    @Override
    public String toString() {
        switch (policyType) {
            case NO_PRODUCT_UNDER_AGE:
                return "NO_PRODUCT_UNDER_AGE { productId=" + productId + ", minAge=" + param + " }";
            case MIN_QTY_PER_PRODUCT:
                return "MIN_QTY_PER_PRODUCT { productId=" + productId + ", minQty=" + param + " }";
            default:
                return "Unknown policy";
        }
    }

    // Static factory methods for easy creation

    public static PurchasePolicy noProductUnderAge(int restrictedProductId, int minBuyerAge) {
        return new PurchasePolicy() {
            @Override
            public boolean isSatisfied(int age, int quantity, int productId) {
                if (age >= minBuyerAge) return true;
                return productId != restrictedProductId;
            }

            @Override
            public String violationMessage() {
                return "Product may be purchased only by users aged " + minBuyerAge + " or older.";
            }

            @Override
            public String toString() {
                return "NO_PRODUCT_UNDER_AGE { productId=" + restrictedProductId + ", minAge=" + minBuyerAge + " }";
            }

            @Override
            public PolicyType getPolicyType() {
                return PolicyType.NO_PRODUCT_UNDER_AGE;
            }
        };
    }

    public static PurchasePolicy minQuantityPerProduct(int restrictedProductId, int minQty) {
        if (minQty < 1) throw new IllegalArgumentException("minQty must be ≥ 1");
        return new PurchasePolicy() {
            @Override
            public boolean isSatisfied(int age, int quantity, int productId) {
                if (productId != restrictedProductId) return true;
                return quantity >= minQty;
            }

            @Override
            public String violationMessage() {
                return "You must purchase at least " + minQty + " of this product.";
            }

            @Override
            public String toString() {
                return "MIN_QTY_PER_PRODUCT { productId=" + restrictedProductId + ", minQty=" + minQty + " }";
            }

            @Override
            public PolicyType getPolicyType() {
                return PolicyType.MIN_QTY_PER_PRODUCT;
            }
        };
    }

}
