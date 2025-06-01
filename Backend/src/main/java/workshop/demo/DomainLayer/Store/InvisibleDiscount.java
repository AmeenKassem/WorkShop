package workshop.demo.DomainLayer.Store;

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
        return isApplicable(scope) ? scope.getTotalPrice() * percent : 0;
    }

    @Override
    public String getName() {
        return name;
    }
}
