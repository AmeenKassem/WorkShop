package workshop.demo.DomainLayer.Store;

import workshop.demo.DomainLayer.User.ShoppingCart;

public class MaxDiscount extends CompositeDiscount{
    public MaxDiscount(String name){
        super(name);
    }
    @Override
    public double apply(ShoppingCart shoppingCart){
        double max=0.0;
        for(Discount discount:subDiscounts){
            double val = discount.apply(shoppingCart);
            if(val>max){
                max=val;
            }
        }
        return max;
    }
}
