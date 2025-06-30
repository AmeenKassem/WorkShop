package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.CreateDiscountDTO;

import java.util.List;

public interface Discount {
    boolean isApplicable(DiscountScope scope);
    double apply(DiscountScope scope);
    String getName();
    default boolean matchesCode(String code){
        return false;
    }
    CreateDiscountDTO toDTO();
    List<Discount> getFlattenedVisibleDiscounts(); // default implementation returning List.of(this) if VisibleDiscount
    String toReadableString(); // Describe what this discount means
    default boolean isLogicalOnly() {
        return false;
    }


}




