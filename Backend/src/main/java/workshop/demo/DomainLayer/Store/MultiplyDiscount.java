package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.CreateDiscountDTO;

import java.util.List;

public class MultiplyDiscount extends CompositeDiscount{
    public MultiplyDiscount(String name){
        super(name);
    }
    @Override
    public boolean isApplicable(DiscountScope scope){
        return discounts.stream().anyMatch(d -> d.isApplicable(scope));
    }
    @Override
    public double apply(DiscountScope scope){
        List<Discount> applicable = discounts.stream()
                .filter(d -> d.isApplicable(scope) && !d.isLogicalOnly())
                .toList();

        if(applicable.isEmpty())
            return 0.0;
        double factor = 1.0;
        for(Discount discount: applicable){
            double percent = discount.apply(scope) / scope.getTotalPrice();
            factor *= (1.0 - percent);
        }
        double overAll = 1.0 - factor;
        return scope.getTotalPrice()*overAll;
    }
    @Override
    public CreateDiscountDTO toDTO() {
        CreateDiscountDTO dto = new CreateDiscountDTO();
        dto.setName(getName());
        dto.setLogic(CreateDiscountDTO.Logic.MULTIPLY);
        dto.setType(CreateDiscountDTO.Type.VISIBLE); // override if needed
        dto.setPercent(0);
        dto.setCondition(conditionString); // ✅ retain original condition

        dto.setSubDiscounts(discounts.stream()
                .map(Discount::toDTO)
                .toList());

        return dto;
    }
}
