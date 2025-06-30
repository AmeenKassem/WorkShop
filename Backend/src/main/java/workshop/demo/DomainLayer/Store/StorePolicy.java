package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DomainLayer.Store.PurchasePolicy.PolicyType;

import java.util.List;

public interface StorePolicy {
boolean isSatisfied(int age, int quantity, int productId);
    String violationMessage();
     PolicyType getPolicyType();
}
