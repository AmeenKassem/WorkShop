package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.CreateDiscountDTO;

import java.util.function.Predicate;

public class DiscountFactory {

    public static Discount fromDTO(CreateDiscountDTO dto) {
        Predicate<DiscountScope> condition = DiscountConditions.fromString(dto.getCondition());

        return switch (dto.getLogic()) {
            case SINGLE -> createLeaf(dto, condition);
            case AND -> {
                AndDiscount and = new AndDiscount(dto.getName());
                dto.getSubDiscounts().forEach(sd -> and.addDiscount(fromDTO(sd)));
                yield and;
            }
            case OR -> {
                OrDiscount or = new OrDiscount(dto.getName());
                dto.getSubDiscounts().forEach(sd -> or.addDiscount(fromDTO(sd)));
                yield or;
            }
            case MAX -> {
                MaxDiscount max = new MaxDiscount(dto.getName());
                dto.getSubDiscounts().forEach(sd -> max.addDiscount(fromDTO(sd)));
                yield max;
            }
            case XOR -> {
                XorDiscount xor = new XorDiscount(dto.getName());
                dto.getSubDiscounts().forEach(sd -> xor.addDiscount(fromDTO(sd)));
                yield xor;
            }
        };
    }

    private static Discount createLeaf(CreateDiscountDTO dto, Predicate<DiscountScope> condition) {
        return switch (dto.getType()) {
            case VISIBLE -> new VisibleDiscount(dto.getName(), dto.getPercent(), condition);
            case INVISIBLE -> new InvisibleDiscount(dto.getName(), dto.getPercent(), condition);
        };
    }
}
