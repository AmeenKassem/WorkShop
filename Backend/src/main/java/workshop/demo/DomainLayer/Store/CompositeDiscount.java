package workshop.demo.DomainLayer.Store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public abstract class CompositeDiscount implements Discount {
    protected final String name;
    protected final List<Discount> discounts = new ArrayList<>();

    // 🔧 ADD: condition support for composite itself
    protected Predicate<DiscountScope> condition = scope -> true;
    protected String conditionString = "None";

    public CompositeDiscount(String name) {
        this.name = name;
    }

    public void addDiscount(Discount discount) {
        discounts.add(discount);
    }

    public void setCondition(Predicate<DiscountScope> condition) {
        this.condition = condition;
    }

    public void setConditionString(String conditionString) {
        this.conditionString = conditionString;
    }

    public Predicate<DiscountScope> getCondition() {
        return condition;
    }

    public String getConditionString() {
        return conditionString;
    }

    @Override
    public String getName() {
        return name;
    }

    public List<Discount> getDiscounts() {
        return discounts;
    }

    // 🔧 FIX: recursive removal with proper handling of parent
    public boolean removeDiscountByName(String name) {
        if (this.name.equals(name)) return true; // allow removing the wrapper itself

        Iterator<Discount> iterator = discounts.iterator();
        while (iterator.hasNext()) {
            Discount d = iterator.next();
            if (d.getName().equals(name)) {
                iterator.remove();
                return true;
            }
            if (d instanceof CompositeDiscount comp && comp.removeDiscountByName(name)) {
                if (comp.getDiscounts().isEmpty()) iterator.remove();
                return true;
            }
        }
        return false;
    }


    // 🔧 FIX: include self in visible flattening
    @Override
    public List<Discount> getFlattenedVisibleDiscounts() {
        List<Discount> result = new ArrayList<>();
        result.add(this); // include the composite itself
        for (Discount d : discounts) {
            result.addAll(d.getFlattenedVisibleDiscounts());
        }
        return result;
    }

    // 🔧 FIX: smarter description
    @Override
    public String toReadableString() {
        return String.format("%s: combination of %d discounts (%s)",
                name,
                discounts.size(),
                DiscountConditions.describe(conditionString)
        );
    }

    @Override
    public boolean isLogicalOnly() {
        return discounts.stream().allMatch(Discount::isLogicalOnly);
    }
}
