package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.CreateDiscountDTO;

import java.util.function.Predicate;

public class InvisibleDiscount implements Discount {
    private final String name;
    private final double percent;
    private final Predicate<DiscountScope> condition;



    public InvisibleDiscount(String name, double percent, Predicate<DiscountScope> condition) {
        this.name = name;
        this.percent = percent;
        this.condition = condition;
    }

    @Override
    public boolean isApplicable(DiscountScope scope) {
        return condition != null && condition.test(scope);
    }

    @Override
    public double apply(DiscountScope scope) {
        String userCode= CouponContext.get();
        if(!matchesCode(userCode)) return 0.0;
        if(!isApplicable(scope)) return 0.0;
        return scope.getTotalPrice() * percent;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public boolean matchesCode(String code){
        return code!=null && name.equals(code);
    }
    public CreateDiscountDTO toDTO() {
        CreateDiscountDTO dto = new CreateDiscountDTO();
        dto.setName(this.name);
        dto.setPercent(this.percent);
        dto.setType(CreateDiscountDTO.Type.VISIBLE);
        dto.setLogic(CreateDiscountDTO.Logic.SINGLE);
        dto.setCondition(condition.toString()); // if overridden properly
        return dto;
    }
}
