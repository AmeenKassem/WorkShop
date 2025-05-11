package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Purchase.ISupplyService;

public class SupplyServiceImp implements ISupplyService {

    private static final Logger logger = LoggerFactory.getLogger(SupplyServiceImp.class);

    public boolean processSupply(SupplyDetails supplyDetails) throws UIException {
        logger.info("processSupply called with supplyDetails: {}", supplyDetails);

        if (supplyDetails.address == null || supplyDetails.city == null || supplyDetails.state == null
                || supplyDetails.zipCode == null) {
            logger.error("Supply failed due to missing fields: {}", supplyDetails);

            throw new UIException("Invalid supply details.", ErrorCodes.SUPPLY_ERROR);
        }
        logger.info("Supply processed successfully for: {}", supplyDetails.address);

        return true;
    }
}
