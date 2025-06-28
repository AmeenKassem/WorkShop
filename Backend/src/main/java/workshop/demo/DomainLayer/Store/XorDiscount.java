package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.CreateDiscountDTO;

import java.util.ArrayList;
import java.util.List;

public class XorDiscount extends CompositeDiscount {
    public XorDiscount(String name) {
        super(name);
    }

    @Override
    public boolean isApplicable(DiscountScope scope) {
        return discounts.stream().filter(d -> d.isApplicable(scope)).count() == 1;
    }

    @Override
    public double apply(DiscountScope scope) {
        List<Discount> applicable = new ArrayList<>();
        for (Discount d : discounts) {
            if (d.isApplicable(scope)) {
                applicable.add(d);
            }
        }

        if (applicable.size() != 1) return 0.0;
        return applicable.get(0).apply(scope);
    }
    @Override
    public CreateDiscountDTO toDTO() {
        CreateDiscountDTO dto = new CreateDiscountDTO();
        dto.setName(getName());
        dto.setLogic(CreateDiscountDTO.Logic.XOR);
        dto.setType(CreateDiscountDTO.Type.VISIBLE);
        dto.setPercent(0); // the discount logic is applied to children
        dto.setCondition("None"); // override if condition is added

        dto.setSubDiscounts(discounts.stream()
                .map(Discount::toDTO)
                .toList());

        return dto;
    }

}
