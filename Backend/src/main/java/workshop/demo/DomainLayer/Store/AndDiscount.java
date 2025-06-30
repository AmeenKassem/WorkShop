package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.CreateDiscountDTO;

public class AndDiscount extends CompositeDiscount {
    public AndDiscount(String name) {
        super(name);
    }

    @Override
    public boolean isApplicable(DiscountScope scope) {
        return condition.test(scope) && discounts.stream().allMatch(d -> d.isApplicable(scope));
    }

    @Override
    public double apply(DiscountScope scope) {
        if (!isApplicable(scope)) return 0.0;

        return discounts.stream()
                .filter(d -> !d.isLogicalOnly())
                .mapToDouble(d -> d.apply(scope))
                .sum();
    }

    @Override
    public CreateDiscountDTO toDTO() {
        CreateDiscountDTO dto = new CreateDiscountDTO();
        dto.setName(getName());
        dto.setLogic(CreateDiscountDTO.Logic.AND);
        dto.setType(CreateDiscountDTO.Type.VISIBLE); // override if needed
        dto.setPercent(0);
        dto.setCondition(conditionString); // ✅ retain original condition

        dto.setSubDiscounts(discounts.stream()
                .map(Discount::toDTO)
                .toList());

        return dto;
    }
}
