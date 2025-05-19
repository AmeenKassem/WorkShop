package workshop.demo.DomainLayer.Store;

import workshop.demo.DomainLayer.User.ShoppingCart;

public class AndDiscount extends CompositeDiscount{
    public AndDiscount(String name){
        super(name);
    }
    @Override
    public double apply(ShoppingCart shoppingCart){
        for(Discount discount : subDiscounts){
            if(discount instanceof HiddenDiscount && ((HiddenDiscount) discount).conditionFails(shoppingCart)){
                return 0.0;
            }
            if(!(discount instanceof HiddenDiscount)&& discount.apply(shoppingCart) == 0.0){
                return 0.0;
            }
        }
        double total=0.0;
        for(Discount discount:subDiscounts){
            if(!(discount instanceof HiddenDiscount)){
                total+=discount.apply(shoppingCart);
            }
        }
        return total;
    }
}
