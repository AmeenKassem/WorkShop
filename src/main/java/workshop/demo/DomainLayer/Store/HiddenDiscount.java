package workshop.demo.DomainLayer.Store;

public class HiddenDiscount implements Discount {

    @Override
    public double apply(double price) {
        return 0.0;
        //Implement this!
    }

    @Override
    public String getDescription() {
        return null;
        //Implement this!
    }
}
