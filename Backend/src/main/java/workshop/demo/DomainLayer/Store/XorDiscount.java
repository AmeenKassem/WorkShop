package workshop.demo.DomainLayer.Store;

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
}
