package workshop.demo.DomainLayer.Store;

public class Store {

    private int stroeID;
    private String storeName;
    private String category;
    //must add something for the stock

    public Store(int storeID, String storeName, String category) {
        this.stroeID = storeID;
        this.storeName = storeName;
        this.category = category;
    }

    public int getStroeID() {
        return stroeID;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getCategory() {
        return category;
    }

}
