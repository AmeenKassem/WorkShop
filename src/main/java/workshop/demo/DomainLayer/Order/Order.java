package workshop.demo.DomainLayer.Order;

import java.util.HashMap;
import java.util.Map;

public class Order {
    private int userId;
    private int storeId; //What if the order contained products from several stores !!!
    //private List<Integer> storesIds; //For example :D 
    private Map<Integer,Integer> productsMap;//key is productID and value is quantity
    public Order(int userId, int storeId){
        this.userId=userId;
        this.storeId=storeId;
        productsMap = new HashMap<Integer,Integer>();
    }
    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }
    public int getStoreId() {
        return storeId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getUserId() {
        return userId;
    }
    public Map<Integer, Integer> getProductsMap() {
        return productsMap;
    }
    public void setProductsMap(Map<Integer, Integer> productsMap) {
        this.productsMap = productsMap;
    }
    //If productId already exists as a key in the map, incerement quantity by 1
    //else add it as a key with quantity 1
    public void addProduct(int productId) {
        productsMap.put(productId, productsMap.getOrDefault(productId, 0) + 1);
    }
    //If productId has value (quantity) more than one, decrease by 1 
    //else remove the key completely    
    public void removeProduct(int productId) {
        if (productsMap.containsKey(productId)) {
            int quantity = productsMap.get(productId);
            if (quantity > 1) {
                productsMap.put(productId, quantity - 1);
            } else {
                productsMap.remove(productId);
            }
        }
    }
    

}
