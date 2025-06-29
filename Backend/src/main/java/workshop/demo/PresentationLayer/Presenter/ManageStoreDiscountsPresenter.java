package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.CreateDiscountDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;

public class ManageStoreDiscountsPresenter {

    private final RestTemplate rest  = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    /*───────────────────────────────────────────────────────────────
     * 1 ▸ Fetch all discount names for a store
     *──────────────────────────────────────────────────────────────*/
    public List<String> fetchDiscountNames(int storeId, String token) {

        try {
            String url = String.format(
                    Base.url + "/api/store/discountNames?storeId=%d&token=%s",
                    storeId,
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8));

            ApiResponse rsp = rest.getForObject(url, ApiResponse.class);

            if (rsp == null || rsp.getErrNumber() != -1) {
                throw new RuntimeException(rsp == null
                        ? "Null response from /discountNames"
                        : rsp.getErrorMsg());
            }
            return Arrays.asList(mapper.convertValue(rsp.getData(), String[].class));

        } catch (Exception ex) {
            ExceptionHandlers.handleException(ex);
            return Collections.emptyList();          // on error: empty list
        }
    }

    /*───────────────────────────────────────────────────────────────
     * 2 ▸ Add / compose a new discount
     *──────────────────────────────────────────────────────────────*/
    public void addDiscount(int storeId, String token,
                            String name, double percent,
                            String type,            // "VISIBLE" | "INVISIBLE"
                            String condition,       // can be ""
                            String logic,           // "SINGLE" | "AND" | …
                            List<String> subNames)  // optional
    {

        try {
            UriComponentsBuilder b = UriComponentsBuilder
                    .fromHttpUrl(Base.url + "/api/store/addDiscount")
                    .queryParam("storeId", storeId)
                    .queryParam("token",  UriUtils.encodeQueryParam(token,  StandardCharsets.UTF_8))
                    .queryParam("name",   UriUtils.encodeQueryParam(name,   StandardCharsets.UTF_8))
                    .queryParam("percent", percent)
                    .queryParam("type",   type)
                    .queryParam("condition",  condition == null ? "" : condition)
                    .queryParam("logic",  logic);

            subNames.forEach(n ->
                    b.queryParam("subDiscountsNames",
                            UriUtils.encodeQueryParam(n, StandardCharsets.UTF_8)));

            ApiResponse rsp = rest.postForObject(b.toUriString(), null, ApiResponse.class);

            if (rsp == null || rsp.getErrNumber() != -1) {
                throw new RuntimeException(rsp == null
                        ? "Null response from /addDiscount"
                        : rsp.getErrorMsg());
            }

        } catch (Exception ex) {
            ExceptionHandlers.handleException(ex);
        }
    }

    /*───────────────────────────────────────────────────────────────
     * 3 ▸ Remove a discount by name
     *──────────────────────────────────────────────────────────────*/
    public void deleteDiscount(int storeId, String token, String name) {

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(Base.url + "/api/store/removeDiscountByName")
                    .queryParam("storeId",      storeId)
                    .queryParam("token",        UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8))
                    .queryParam("discountName", UriUtils.encodeQueryParam(name,  StandardCharsets.UTF_8))
                    .toUriString();

            ApiResponse rsp = rest.postForObject(url, null, ApiResponse.class);

            if (rsp == null || rsp.getErrNumber() != -1) {
                throw new RuntimeException(rsp == null
                        ? "Null response from /removeDiscountByName"
                        : rsp.getErrorMsg());
            }

        } catch (Exception ex) {
            ExceptionHandlers.handleException(ex);
        }
    }
    public List<CreateDiscountDTO> fetchDiscountDetails(int storeId, String token) {
        try {
            String url = String.format(
                    Base.url + "/api/store/getStoreDiscounts?storeId=%d&token=%s",
                    storeId,
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8));

            ApiResponse rsp = rest.getForObject(url, ApiResponse.class);

            if (rsp == null || rsp.getErrNumber() != -1) {
                throw new RuntimeException(rsp == null
                        ? "Null response from /getStoreDiscounts"
                        : rsp.getErrorMsg());
            }

            return Arrays.asList(mapper.convertValue(rsp.getData(), CreateDiscountDTO[].class));

        } catch (Exception ex) {
            ExceptionHandlers.handleException(ex);
            return Collections.emptyList();
        }
    }
}
