package workshop.demo.DomainLayer.Store;

public interface Discount {
    double apply(double price);
    String getDescription();

}