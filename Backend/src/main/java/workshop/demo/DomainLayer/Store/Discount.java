package workshop.demo.DomainLayer.Store;

public interface Discount {
    boolean isApplicable(DiscountScope scope);
    double apply(DiscountScope scope);
    String getName();
}




