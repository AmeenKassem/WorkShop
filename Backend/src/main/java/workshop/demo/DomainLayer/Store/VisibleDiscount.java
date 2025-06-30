package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.CreateDiscountDTO;

import java.util.List;
import java.util.function.Predicate;

public class VisibleDiscount implements Discount {
    private final String name;
    private final double percent;
    private final Predicate<DiscountScope> condition;
    private final String conditionString;

    public VisibleDiscount(String name, double percent, Predicate<DiscountScope> condition, String conditionString) {
        this.name = name;
        this.percent = percent;
        this.condition = condition;
        this.conditionString = conditionString;
    }


    @Override
    public boolean isApplicable(DiscountScope scope) {
        return condition == null || condition.test(scope);
    }

    @Override
    public double apply(DiscountScope scope) {
        if (!isApplicable(scope)) return 0.0;
        return scope.getTotalPrice() * percent;
    }

    @Override
    public String getName() {
        return name;
    }

    public CreateDiscountDTO toDTO() {
        CreateDiscountDTO dto = new CreateDiscountDTO();
        dto.setName(this.name);
        dto.setPercent(this.percent);
        dto.setType(CreateDiscountDTO.Type.VISIBLE);
        dto.setLogic(CreateDiscountDTO.Logic.SINGLE);
        dto.setCondition(this.conditionString); // ✅ use actual string, not lambda

        return dto;
    }

    public double getPercent() {
        return percent;
    }

    public Predicate<DiscountScope> getCondition() {
        return condition;
    }

    public String getConditionString() {
        return conditionString;
    }
    @Override
    public List<Discount> getFlattenedVisibleDiscounts() {
        return List.of(this);
    }

    @Override
    public String toReadableString() {
        return String.format("%s: %.0f%% off when %s", name, percent*100, DiscountConditions.describe(conditionString));
    }
    @Override
    public boolean isLogicalOnly() {
        return percent == 0;
    }

}
