package workshop.demo.DomainLayer.Store;

public class NormalDiscount implements Discount {
    //Be aware to use percent as intended (e.g. discount is 20% then percent is 0.2)
    private final double percent;
    public NormalDiscount(double percent){
        this.percent=percent;
    }
    @Override
    public double apply(double price){
        return price * (1-percent);
    }
    @Override
    public String getDescription(){
        return ((int)(percent*100)) + "% off";
    }

}