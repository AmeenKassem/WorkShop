package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;

import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.NotificationView;
import workshop.demo.PresentationLayer.View.PurchaseView;

public class PurchasePresenter {

    private final PurchaseView view;
    private final RestTemplate restTemplate = new RestTemplate();

    public PurchasePresenter(PurchaseView view) {
        this.view = view;
    }

    public void submitPurchase(
            String cardNumber,
            String cardHolderName,
            String expirationDate,
            String cvv,
            String address,
            String city,
            String state,
            String zipCode) {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            NotificationView.showError(ExceptionHandlers.getErrorMessage(1001));
            return;
        }

        try {
            // Create objects
            PaymentDetails paymentDetails = new PaymentDetails(cardNumber, cardHolderName, expirationDate, cvv);
            SupplyDetails supplyDetails = new SupplyDetails(address, city, state, zipCode);

            // Serialize to JSON
            ObjectMapper mapper = new ObjectMapper();
            String paymentJson = mapper.writeValueAsString(paymentDetails);
            String supplyJson = mapper.writeValueAsString(supplyDetails);

            // URL-encode the JSON strings
            String encodedPaymentJson = UriUtils.encodeQueryParam(paymentJson, StandardCharsets.UTF_8);
            String encodedSupplyJson = UriUtils.encodeQueryParam(supplyJson, StandardCharsets.UTF_8);

            Object uesrtype = VaadinSession.getCurrent().getAttribute("userType");
            String url;
            if ("user".equals(uesrtype)) {
                // Build request URL
                url = String.format(
                        "http://localhost:8080/purchase/registered?token=%s&paymentJson=%s&supplyJson=%s",
                        UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                        encodedPaymentJson,
                        encodedSupplyJson);
            } else {
                // Build request URL
                url = String.format(
                        "http://localhost:8080/purchase/guest?token=%s&paymentJson=%s&supplyJson=%s",
                        UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                        encodedPaymentJson,
                        encodedSupplyJson);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<List<ReceiptDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<List<ReceiptDTO>>>() {
                    });

            ApiResponse<List<ReceiptDTO>> responseBody = response.getBody();

            if (responseBody != null && responseBody.getErrNumber() == -1) {
                List<ReceiptDTO> receiptList = responseBody.getData();
                ReceiptDTO[] receipts = receiptList.toArray(new ReceiptDTO[0]);

                PurchaseView.showReceiptDialog(receipts);
                UI.getCurrent().navigate("");
            } else {

                NotificationView.showError(ExceptionHandlers.getErrorMessage(responseBody.getErrNumber()));
            }

        } catch (Exception e) {

            ExceptionHandlers.handleException(e);
            // NotificationView.showError("Failed to complete purchase: " + e.getMessage());
        }
    }
}
