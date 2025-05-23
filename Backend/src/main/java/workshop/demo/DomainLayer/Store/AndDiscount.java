package workshop.demo.DomainLayer.Store;

public class AndDiscount extends CompositeDiscount {
    public AndDiscount(String name) {
        super(name);
    }

    @Override
    public boolean isApplicable(DiscountScope scope) {
        return discounts.stream().allMatch(d -> d.isApplicable(scope));
    }

    @Override
    public double apply(DiscountScope scope) {
        if (!isApplicable(scope)) return 0.0;

        return discounts.stream()
                .mapToDouble(d -> d.apply(scope))
                .sum();
    }
}
