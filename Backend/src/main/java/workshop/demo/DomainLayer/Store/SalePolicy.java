package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DomainLayer.Store.PurchasePolicy.PolicyType;

import java.util.List;

public class SalePolicy implements StorePolicy {

    @Override
            public boolean isSatisfied(int age, int quantity, int productId) {
        return false;
    }

    @Override
    public String violationMessage() {
        return null;
    }
    public PolicyType getPolicyType() {
        return PolicyType.SALE_POLICY; // Assuming SALE_POLICY is a valid enum value in PolicyType
    }
}
