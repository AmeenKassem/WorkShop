package workshop.demo.DomainLayer.Store;

import java.util.List;

public class MultiplyDiscount extends CompositeDiscount{
    public MultiplyDiscount(String name){
        super(name);
    }
    @Override
    public boolean isApplicable(DiscountScope scope){
        return discounts.stream().anyMatch(d -> d.isApplicable(scope));
    }
    @Override
    public double apply(DiscountScope scope){
        List<Discount> applicable = discounts.stream().filter(d -> d.isApplicable(scope)).toList();
        if(applicable.isEmpty())
            return 0.0;
        double factor = 1.0;
        for(Discount discount: applicable){
            double percent = discount.apply(scope) / scope.getTotalPrice();
            factor *= (1.0 - percent);
        }
        double overAll = 1.0 - factor;
        return scope.getTotalPrice()*overAll;
    }
}
