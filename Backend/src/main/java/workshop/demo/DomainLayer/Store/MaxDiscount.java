package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.CreateDiscountDTO;

public class MaxDiscount extends CompositeDiscount {
    public MaxDiscount(String name) {
        super(name);
    }

    @Override
    public boolean isApplicable(DiscountScope scope) {
        return discounts.stream().anyMatch(d -> d.isApplicable(scope));
    }

    @Override
    public double apply(DiscountScope scope) {
        return discounts.stream()
                .filter(d -> d.isApplicable(scope))
                .mapToDouble(d -> d.apply(scope))
                .max()
                .orElse(0.0);
    }
    @Override
    public CreateDiscountDTO toDTO() {
        CreateDiscountDTO dto = new CreateDiscountDTO();
        dto.setName(getName());
        dto.setLogic(CreateDiscountDTO.Logic.MAX);
        dto.setType(CreateDiscountDTO.Type.VISIBLE);
        dto.setPercent(0); // the discount logic is applied to children
        dto.setCondition("None"); // override if condition is added

        dto.setSubDiscounts(discounts.stream()
                .map(Discount::toDTO)
                .toList());

        return dto;
    }
}
