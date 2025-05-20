package workshop.demo.DomainLayer.Store;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeDiscount implements Discount {
    protected final String name;
    protected final List<Discount> discounts = new ArrayList<>();

    public CompositeDiscount(String name) {
        this.name = name;
    }

    public void addDiscount(Discount discount) {
        discounts.add(discount);
    }

    @Override
    public String getName() {
        return name;
    }
}
