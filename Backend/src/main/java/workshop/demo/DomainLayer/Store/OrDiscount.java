package workshop.demo.DomainLayer.Store;

import workshop.demo.DomainLayer.User.ShoppingCart;

public class OrDiscount extends CompositeDiscount{
    public OrDiscount(String name){
        super(name);
    }
    @Override
    public double apply(ShoppingCart shoppingCart) {
        double total = 0.0;
        for (Discount discount : subDiscounts) {
            total += discount.apply(shoppingCart);
        }
        return total;
    }
}
