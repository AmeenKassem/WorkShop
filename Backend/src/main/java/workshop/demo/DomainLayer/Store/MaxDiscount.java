package workshop.demo.DomainLayer.Store;

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
}
