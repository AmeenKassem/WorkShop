package workshop.demo.DomainLayer.Store;

import workshop.demo.DomainLayer.User.ShoppingCart;

public class XorDiscount extends CompositeDiscount{
    public XorDiscount(String name){
        super(name);
    }
    @Override //CHECK THIS !
    public double apply(ShoppingCart shoppingCart) {
        double best = 0.0;
        int count = 0;
        for (Discount discount : subDiscounts) {
            double val = discount.apply(shoppingCart);
            if (val > 0.0) {
                count++;
                if (val > best) best = val;
            }
        }
        return best;
    }
}
