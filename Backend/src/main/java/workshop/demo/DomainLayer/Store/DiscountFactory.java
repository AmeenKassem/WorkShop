package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.CreateDiscountDTO;

import java.util.function.Predicate;

public class DiscountFactory {

    public static Discount fromDTO(CreateDiscountDTO dto) {
        Discount base;
        Predicate<DiscountScope> condition = DiscountConditions.fromString(dto.getCondition());
        switch (dto.getLogic()) {
            case SINGLE -> base = createLeaf(dto,condition);
            case AND, OR, XOR, MAX, MULTIPLY -> {
                CompositeDiscount comp = createComposite(dto.getLogic(), dto.getName());
                for (CreateDiscountDTO subDto : dto.getSubDiscounts()) {
                    comp.addDiscount(fromDTO(subDto)); // recursive call
                }
                base = comp;
            }
            default -> throw new IllegalArgumentException("Unknown logic");
        }
        return base;
    }


    private static Discount createLeaf(CreateDiscountDTO dto, Predicate<DiscountScope> condition) {
        return switch (dto.getType()) {
            case VISIBLE -> new VisibleDiscount(dto.getName(), dto.getPercent(), condition, dto.getCondition());
            case INVISIBLE -> new InvisibleDiscount(dto.getName(), dto.getPercent(), condition, dto.getCondition());
        };
    }
    public static CompositeDiscount createComposite(CreateDiscountDTO.Logic logic, String name) {
        return switch (logic) {
            case AND -> new AndDiscount(name);
            case OR -> new OrDiscount(name);
            case XOR -> new XorDiscount(name);
            case MAX -> new MaxDiscount(name);
            case MULTIPLY -> new MultiplyDiscount(name);
            default -> throw new IllegalArgumentException("Not a composite logic: " + logic);
        };
    }


}
