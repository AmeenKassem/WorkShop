package workshop.demo.ApplicationLayer.DataInitilizer;

import java.util.List;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;

import org.springframework.stereotype.Component;

@Component
public class StoreParser extends ManagerDataInit {

    public void store(List<String> construction) {
        List<String> toSend = construction.subList(1, construction.size());
        switch (construction.getFirst()) {
            case "open":
                createStore(toSend);
                break;
            case "item":
                addItem(toSend);
                break;
            case "auction":
                auction(toSend);
                break;
            default:
                log("undefined function for store on line " + line + " : " + construction.getFirst());
                error = true;
                break;
        }

    }

    private void auction(List<String> toSend) {
        if (toSend.size() != 6) {
            log("syntax errror with auction , params not match : auction <username> <store name> <product name> <auction time> <quantity>  <start price> ; .line:"
                    + line);
            error = true;
            return;
        }
        String token = getTokenForUserName(toSend.get(0));
        if (token == null) {
            return;
        }
        String storeName = toSend.get(1).replace("-", " ");
        int id = getStoreIdByName(storeName);
        String productName = toSend.get(2).replace("-", " ");
        long time = Long.parseLong(toSend.get(3));
        int quantity = Integer.parseInt(toSend.get(4));
        double startPrice = Double.parseDouble(toSend.get(5));
        try {
            ItemStoreDTO item = getProductByNameAndStore(id, productName,token,storeName);
            activeService.setProductToAuction(token, id, item.getProductId(), quantity, time, startPrice);
            // entityManager.flush();
            log("auction set success!!");
            
        } catch (Exception e) {
            log("line "+line+" got error :"+e.getMessage());
        }
    }

    

    private void addItem(List<String> toSend) {
        if (toSend.size() < 8) {
            log("forgot one param or more :  #ownerName #stroeName #productName #productCategory #desc #quantity #price #keword1 #keyword2 ... ; ");
            error = true;
            return;
        }
        String token = getTokenForUserName(toSend.get(0));
        if (token == null) {
            return;
        }
        String storeName = toSend.get(1).replace("-", " ");
        int id = getStoreIdByName(storeName);
        String productName = toSend.get(2).replace("-", " ");
        Category category = Category.valueOf(toSend.get(3));
        String desc = toSend.get(4).replace("-", " ");
        int quantity = Integer.parseInt(toSend.get(5));
        int price = Integer.parseInt(toSend.get(6));
        String[] keyWords = new String[toSend.size() - 7];
        for (int i = 0; i < toSend.size() - 7; i++) {
            keyWords[i] = toSend.get(i + 7).replace("-", " ");
        }
        try {
            int productId = stockService.addProduct(token, productName, category, desc, keyWords);
            stockService.addItem(id, token, productId, quantity, price, category);
            log("successfuly adding " + productName + " on store " + storeName + "! ");
        } catch (Exception e) {
            log("error on adding item/product " + line + " : " + e.getMessage());
            error = true;
        }

    }

    protected void createStore(List<String> toSend) {
        if (toSend.size() != 3) {
            log("params not sufficent " + toSend.size() + " , must : store create #storeName #storeCategory");
            error = true;
            return;
        }
        String userName = toSend.get(0);
        String storeName = toSend.get(1).replace("-", " ");
        String category = toSend.get(2).replace("-", " ");
        if (!tokens.containsKey(userName)) {
            error = true;
            log("you must login using B-Script");
            return;
        }
        String token = tokens.get(userName);
        try {
            int storeId = storeService.addStoreToSystem(token, storeName, category);
            ids.put(storeName, storeId);
            log("successfuly create store " + storeName);
        } catch (UIException | DevException e) {
            error = true;
            log("got error on line " + line + " :" + e.getMessage());
        }

    }
}
