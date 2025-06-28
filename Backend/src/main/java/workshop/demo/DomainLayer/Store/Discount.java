package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.CreateDiscountDTO;

public interface Discount {
    boolean isApplicable(DiscountScope scope);
    double apply(DiscountScope scope);
    String getName();
    default boolean matchesCode(String code){
        return false;
    }
    CreateDiscountDTO toDTO();

}




