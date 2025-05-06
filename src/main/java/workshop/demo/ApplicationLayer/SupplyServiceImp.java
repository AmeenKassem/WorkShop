package workshop.demo.ApplicationLayer;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Purchase.ISupplyService;

public class SupplyServiceImp implements ISupplyService {

    public boolean processSupply(SupplyDetails supplyDetails) throws Exception {
    if (supplyDetails.address == null || supplyDetails.city == null || supplyDetails.state == null || supplyDetails.zipCode == null) {
                throw new Exception("invalid supply details.");
    }
         
    return true;
    }
}
