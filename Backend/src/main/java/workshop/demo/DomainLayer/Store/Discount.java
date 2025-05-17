package workshop.demo.DomainLayer.Store;

import workshop.demo.DomainLayer.User.ShoppingCart;

public abstract class Discount {
    protected String name;
    public Discount(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
    public abstract double apply(ShoppingCart shoppingCart);
}