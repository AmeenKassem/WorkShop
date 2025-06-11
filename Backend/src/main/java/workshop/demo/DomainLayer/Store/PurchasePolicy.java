package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.UserDTO;

import java.util.List;

public interface PurchasePolicy extends StorePolicy {
    static PurchasePolicy noAlcoholUnder18() {
        return new PurchasePolicy() {
            private static final String CATEGORY = "ALCOHOL";
            @Override
            public boolean isSatisfied(UserDTO buyer, List<ItemStoreDTO> cart) {
                if (buyer.getAge() >= 18) return true;
                return cart.stream()
                        .noneMatch(i -> CATEGORY.equalsIgnoreCase(i.getCategory().name()));
            }
            @Override
            public String violationMessage() {
                return "Alcoholic beverages may be sold only to users aged 18 or older.";
            }
        };
    }
    static PurchasePolicy minQuantityPerProduct(int minQty) {
        if (minQty < 1) throw new IllegalArgumentException("minQty must be ≥ 1");
        return new PurchasePolicy() {
            @Override
            public boolean isSatisfied(UserDTO buyer, List<ItemStoreDTO> cart) {
                return cart.stream().allMatch(i -> i.getQuantity() >= minQty);
            }
            @Override
            public String violationMessage() {
                return "Each product in the basket must be purchased in a quantity of at least " + minQty + ".";
            }
        };
    }

}
