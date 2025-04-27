package workshop.demo.DomainLayer.Store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.Product;

public class Store {

    private int stroeID;
    private String storeName;
    private String category;
    private boolean active;
    private Map<Product.Category, List<item>> stock;//map of category -> item
    private ActivePurcheses activePurchases;
    //must add something for messages

    public Store(int storeID, String storeName, String category) {
        this.stroeID = storeID;
        this.storeName = storeName;
        this.category = category;
        this.active = true;
        stock = new HashMap<>();
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    // add product 
    // remove product -> quantity=0
    // update price
    // rank product 
    //display products in store
    // search product by name 
    //search product by category 


    public SingleBid bidOnAuctionProduct(int auctionId, int userId,double price) throws Exception{
        return activePurchases.addUserBidToAuction(auctionId, userId, price);
    }

    public int addProductToAuction(int productId,int quantity,double startPrice , long time) throws Exception{
        item item = getItemById(productId);
        if(item.getQuantity()<quantity){
            throw new UIException("stock not enought to make this auction .");
        }
        item.setQuantity(item.getQuantity()-quantity);
        return activePurchases.addProductToAuction(productId, quantity, time);

    }

    private item getItemById(int productId){
        for (List<item> category : stock.values()) {
            for (item item : category) {
                if(item.getProdutId()==productId)
                    return item;
            }
        }
        return null;
    }

    public AuctionDTO[] getAllAuctions() {
       return activePurchases.getAuctions();
    }

}
