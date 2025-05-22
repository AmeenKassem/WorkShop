package workshop.demo.DomainLayer.Store;

public class OrDiscount extends CompositeDiscount {
    public OrDiscount(String name) {
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
                .findFirst()
                .map(d -> d.apply(scope))
                .orElse(0.0);

    }
}
