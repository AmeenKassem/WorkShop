package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.ShoppingCart;

import java.util.function.Predicate;

public class CombinedDiscount extends CompositeDiscount{
    public CombinedDiscount(String name){
        super(name);
    }
    @Override
    public double apply(ShoppingCart shoppingCart){
        double totalDiscount = 0.0;
        for(ItemCartDTO item: shoppingCart.getAllCart()){
            double original= item.price*item.quantity;
            double factor= 1.0;
            for(Discount discount: subDiscounts){
                if (discount instanceof NormalDiscount normalDiscount) {
                    String targetCategory = normalDiscount.getTargetCategory();
                    Integer targetProductId = normalDiscount.getTargetProductId();
                    Predicate<ShoppingCart> condition = normalDiscount.getCondition();
                    if ((targetCategory == null || targetCategory.equals(item.category.name())) &&
                            (targetProductId == null || targetProductId == item.productId) &&
                            (condition == null || condition.test(shoppingCart))) {
                        factor *= (1 - normalDiscount.getPercent());
                    }
                }
            }
            totalDiscount+=original*(1-factor);
        }
        return totalDiscount;
    }
}
