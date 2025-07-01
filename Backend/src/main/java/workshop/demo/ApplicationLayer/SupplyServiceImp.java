package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Purchase.ISupplyService;

@Service
public class SupplyServiceImp implements ISupplyService {

    private static final Logger logger = LoggerFactory.getLogger(SupplyServiceImp.class);

    @Override
    public int processSupply(SupplyDetails supplyDetails) throws UIException {
        logger.info("processSupply called with supplyDetails: {}", supplyDetails);

        if (supplyDetails.name == null || supplyDetails.address == null
                || supplyDetails.city == null || supplyDetails.country == null
                || supplyDetails.zipCode == null) {
            logger.error("Supply failed due to missing fields: {}", supplyDetails);
            System.out.println(supplyDetails.name + "-" + supplyDetails.address + "-" + supplyDetails.city + "-" + supplyDetails.country + "-" + supplyDetails.zipCode);
            throw new UIException("Missing supply details", ErrorCodes.SUPPLY_ERROR);
        }
        //return 1001;
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("action_type", "supply");
        params.add("name", supplyDetails.name);
        params.add("address", supplyDetails.address);
        params.add("city", supplyDetails.city);
        params.add("country", supplyDetails.country);
        params.add("zip", supplyDetails.zipCode);

        String result = postToExternalSystem(params);
        try {
            int transactionId = Integer.parseInt(result);
            if (transactionId >= 10000 && transactionId <= 100000) {
                logger.info("Supply successful. Transaction ID: {}", transactionId);
                return transactionId;
            } else {
                logger.warn("Supply failed. Received transaction ID: {}", transactionId);
                return -1;
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid response from external supply system: {}", result);
            throw new UIException("External supply failed", ErrorCodes.SUPPLY_ERROR);
        }
    }

    private String postToExternalSystem(MultiValueMap<String, String> params) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        String url = "https://damp-lynna-wsep-1984852e.koyeb.app/";
        return restTemplate.postForObject(url, request, String.class);
    }

}
