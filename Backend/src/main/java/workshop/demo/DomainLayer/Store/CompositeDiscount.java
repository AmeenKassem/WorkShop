package workshop.demo.DomainLayer.Store;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeDiscount extends Discount {
    protected final List<Discount> subDiscounts = new ArrayList<>();
    public CompositeDiscount(String name){
        super(name);
    }
    public void addDiscount(Discount discount){
        subDiscounts.add(discount);
    }
}
