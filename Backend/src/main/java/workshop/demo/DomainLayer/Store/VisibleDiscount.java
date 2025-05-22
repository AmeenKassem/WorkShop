package workshop.demo.DomainLayer.Store;

import java.util.function.Predicate;

public class VisibleDiscount implements Discount {
    private final String name;
    private final double percent;
    private final Predicate<DiscountScope> condition;

    public VisibleDiscount(String name, double percent, Predicate<DiscountScope> condition) {
        this.name = name;
        this.percent = percent;
        this.condition = condition;
    }

    @Override
    public boolean isApplicable(DiscountScope scope) {
        boolean x = condition == null || condition.test(scope);
        System.out.println(x);
        return condition == null || condition.test(scope);
    }

    @Override
    public double apply(DiscountScope scope) {
        if (!isApplicable(scope)) return 0.0;
        return scope.getTotalPrice() * (percent / 100.0); // ✅ correct
    }


    @Override
    public String getName() {
        return name;
    }
}
