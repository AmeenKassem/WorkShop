package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import workshop.demo.Controllers.ApiResponse;

public class ManageStoreDiscountsPresenter {

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Returns every discount name currently defined for the given store.
     * Throws Exception if the backend responds with an error.
     */
    public List<String> fetchDiscountNames(int storeId, String token) throws Exception {
        String url = String.format(
                "http://localhost:8080/api/store/discountNames?storeId=%d&token=%s",
                storeId,
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8));


        ApiResponse rsp = rest.getForObject(url, ApiResponse.class);
        if (rsp == null || rsp.getErrNumber() != -1) {
            throw new Exception(rsp == null ? "null response" : rsp.getErrorMsg());
        }
        return Arrays.asList(mapper.convertValue(rsp.getData(), String[].class));
    }
    public void addDiscount(int storeId, String token,
                            String name, double percent,
                            String type,      // "VISIBLE" | "INVISIBLE"
                            String condition,
                            String logic,     // "SINGLE" | "AND" | …
                            List<String> subNames) throws Exception {

        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl("http://localhost:8080/api/store/addDiscount")
                .queryParam("storeId", storeId)
                .queryParam("token",  UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8))
                .queryParam("name",   UriUtils.encodeQueryParam(name,  StandardCharsets.UTF_8))
                .queryParam("percent", percent)
                .queryParam("type",   type)
                .queryParam("condition",
                        UriUtils.encodeQueryParam(condition == null ? "" : condition, StandardCharsets.UTF_8))
                .queryParam("logic",  logic);

        subNames.forEach(n ->
                b.queryParam("subDiscountsNames",
                        UriUtils.encodeQueryParam(n, StandardCharsets.UTF_8)));

        String url = b.build(true).toUriString();

        ApiResponse rsp = rest.postForObject(url, null, ApiResponse.class);
        if (rsp == null || rsp.getErrNumber() != -1) {
            throw new Exception(rsp == null ? "null response" : rsp.getErrorMsg());
        }
    }


}
