package workshop.demo.ApplicationLayer;

import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Purchase.ISupplyService;

public class SupplyServiceImp implements ISupplyService {

    public boolean processSupply(SupplyDetails supplyDetails) throws UIException {
        if (supplyDetails.address == null || supplyDetails.city == null || supplyDetails.state == null || supplyDetails.zipCode == null) {
            throw new UIException("Invalid supply details.", ErrorCodes.SUPPLY_ERROR);
        }
        return true;
    }
}
