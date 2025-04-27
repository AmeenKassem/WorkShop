package workshop.demo.ApplicationLayer;

import workshop.demo.DTOs.RecieptDTO;

public class PurchaseService {




    public RecieptDTO buyGuestCart(String token){
        try {
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            int ownerID = authRepo.getUserId(token);
        //check validate token
        //get id from token
        //get shoping cart of guest
        //check avalibality
        //get sum 
        //pay 
        //supply 
        //add order to order history
    }

}
