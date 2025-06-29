package workshop.demo.ApplicationLayer.DataInitilizer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import workshop.demo.ApplicationLayer.ReviewService;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.item;

public class ReviewParser extends ManagerDataInit {
    
    @Autowired
    private ReviewService reviewService;

    public void review(List<String> toSend){
        // List<String> toSend = construction.subList(1, construction.size());
        String token = getTokenForUserName(toSend.get(1));
        int storeId = getStoreIdByName(toSend.get(2).replace("-", " "));
        String msg = toSend.get(toSend.size()-1).replace("-"," ");
        if(toSend.get(0).equals("store")){
            try {
                reviewService.AddReviewToStore(token, storeId, msg);
                log("reviw "+msg+" on store "+toSend.get(2)+" success");
            } catch (UIException e) {
                log("error on set reviw on store "+e.getMessage());
            }
        }else if(toSend.get(0).equals("product")){
            try {
                ItemStoreDTO item = getProductByNameAndStore(storeId, toSend.get(3), token, msg);
                reviewService.AddReviewToProduct(token, storeId, item.getProductId(), msg);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
