package workshop.demo.DomainLayer.Store;

import workshop.demo.DomainLayer.User.ShoppingCart;

import java.util.function.Predicate;

public class HiddenDiscount extends Discount {
    private final Predicate<ShoppingCart> condition;
    public HiddenDiscount(Predicate<ShoppingCart> condition){
        super("Hidden");
        this.condition = condition;
    }
    @Override
    public double apply(ShoppingCart shoppingCart){
        return condition.test(shoppingCart) ? 0.0 : 0.0;
    }
    public boolean conditionFails(ShoppingCart shoppingCart){
        return !condition.test(shoppingCart);
    }
}