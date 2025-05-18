package workshop.demo.DomainLayer.Store;

import org.springframework.beans.factory.config.Scope;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.ShoppingCart;

import java.util.function.Predicate;

public class NormalDiscount extends Discount {
    //Be aware to use percent as intended (e.g. discount is 20% then percent is 0.2)
    private final double percent;
    private final String targetCategory;
    private final Integer targetProductId;
    private Predicate<ShoppingCart> condition;
    public NormalDiscount(String name,double percent, String targetCategory, Integer targetProductId, Predicate<ShoppingCart> condition) {
        super(name);
        this.percent = percent;
        this.targetCategory = targetCategory;
        this.targetProductId = targetProductId;
        this.condition = condition;
    }
    @Override
    public double apply(ShoppingCart shoppingCart){
        if(condition != null && !condition.test(shoppingCart)){
            return 0.0;
        }
        double discount = 0.0;
        for(ItemCartDTO item: shoppingCart.getAllCart()){
            if ((targetCategory == null || targetCategory.equals(item.category.name())) &&
                    (targetProductId == null || targetProductId == item.productId)) {
                discount += item.price * item.quantity * percent;
            }
        }
        return discount;
    }
    public double getPercent(){
        return percent;
    }
    public String getTargetCategory(){
        return targetCategory;
    }
    public Integer getTargetProductId(){
        return targetProductId;
    }
    public Predicate<ShoppingCart> getCondition(){
        return condition;
    }

}