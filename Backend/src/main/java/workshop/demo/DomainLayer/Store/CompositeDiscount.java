package workshop.demo.DomainLayer.Store;

import java.util.ArrayList;
import java.util.Iterator;
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
    public boolean removeDiscountByName(String name) {
        Iterator<Discount> iterator = discounts.iterator();
        while (iterator.hasNext()) {
            Discount d = iterator.next();
            if (d.getName().equals(name)) {
                iterator.remove();
                return true;
            }
            if (d instanceof CompositeDiscount composite) {
                if (composite.removeDiscountByName(name)) {
                    if (composite.getDiscounts().isEmpty()) {
                        iterator.remove();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public List<Discount> getDiscounts() {
        return discounts;
    }
    @Override
    public List<Discount> getFlattenedVisibleDiscounts() {
        List<Discount> result = new ArrayList<>();
        for (Discount d : discounts) {
            result.addAll(d.getFlattenedVisibleDiscounts());
        }
        return result;
    }
    @Override
    public String toReadableString() {
        return String.format("%s (composite %s with %d sub-discounts)",
                name, this.getClass().getSimpleName().replace("Discount", ""), discounts.size());
    }



}
