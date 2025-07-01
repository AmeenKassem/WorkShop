package workshop.demo.DomainLayer.Store;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "policy_manager")
public class PolicyManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Link to owning Store (one-to-one)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", unique = true, nullable = false)
    private Store store;

    @OneToMany(mappedBy = "policyManager", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PurchasePolicy> purchasePolicies = new HashSet<>();

    public PolicyManager() {
        // JPA no-arg constructor
    }

    public PolicyManager(Store store) {
        this.store = store;
    }

    public int getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Set<PurchasePolicy> getPurchasePolicies() {
        return purchasePolicies;
    }

    public void setPurchasePolicies(Set<PurchasePolicy> purchasePolicies) {
        this.purchasePolicies = purchasePolicies;
    }

    public void addPolicy(PurchasePolicy policy) {
        purchasePolicies.add(policy);
        policy.setPolicyManager(this);
        policy.setPolicyType(policy.getPolicyType()); 
    }

   public boolean removePolicy(PurchasePolicy.PolicyType type, int productId, int param) {
    PurchasePolicy toRemove = null;
    for (PurchasePolicy policy : purchasePolicies) {
        if (policy.getPolicyType() == type
                && policy.getProductId() == productId) {
            toRemove = policy;
            break;
        }
    }
    if (toRemove != null) {
        purchasePolicies.remove(toRemove);
        toRemove.setPolicyManager(null);
        return true;  // indicates successful removal
    }
    return false;  // policy not found
}

    public boolean validate(int age, int quantity, int productId) {
        for (PurchasePolicy policy : purchasePolicies) {
            if (!policy.isSatisfied(age, quantity, productId)) {
                return false;
            }
        }
        return true;
    }

    public Set<String> getViolationMessages(int age, int quantity, int productId) {
        Set<String> violations = new HashSet<>();
        for (PurchasePolicy policy : purchasePolicies) {
            if (!policy.isSatisfied(age, quantity, productId)) {
                violations.add(policy.violationMessage());
            }
        }
        return violations;
    }

    public void assertPolicies(int age, int quantity, int productId) {
        for (PurchasePolicy policy : purchasePolicies) {
            if (!policy.isSatisfied(age, quantity, productId)) {
                throw new IllegalStateException(policy.violationMessage());
            }
        }
    }

    @Override
    public String toString() {
        return "PolicyManagerEntity{" +
               "storeId=" + (store != null ? store.getstoreId() : "null") +
               ", policies=" + purchasePolicies +
               '}';
    }
}
