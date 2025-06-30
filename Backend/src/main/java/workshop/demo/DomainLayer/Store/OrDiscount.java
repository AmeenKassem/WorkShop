package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.CreateDiscountDTO;

public class OrDiscount extends CompositeDiscount {
    public OrDiscount(String name) {
        super(name);
    }

    @Override
    public boolean isApplicable(DiscountScope scope) {
        return discounts.stream().anyMatch(d -> d.isApplicable(scope));
    }

    @Override
    public double apply(DiscountScope scope) {
        return discounts.stream()
                .filter(d -> d.isApplicable(scope) && !d.isLogicalOnly())
                .mapToDouble(d -> d.apply(scope))
                .sum();
    }

    @Override
    public CreateDiscountDTO toDTO() {
        CreateDiscountDTO dto = new CreateDiscountDTO();
        dto.setName(getName());
        dto.setLogic(CreateDiscountDTO.Logic.OR);
        dto.setType(CreateDiscountDTO.Type.VISIBLE); // override if needed
        dto.setPercent(0);
        dto.setCondition(conditionString); // ✅ retain original condition

        dto.setSubDiscounts(discounts.stream()
                .map(Discount::toDTO)
                .toList());

        return dto;
    }
}