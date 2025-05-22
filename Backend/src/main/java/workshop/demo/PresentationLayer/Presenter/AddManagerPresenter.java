package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.AddManagerView;

public class AddManagerPresenter {

    private final AddManagerView view;
    private final RestTemplate restTemplate = new RestTemplate();
    private int storeId;

    public AddManagerPresenter(AddManagerView view) {
        this.view = view;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public void sendAddManagerRequest(String managerName, Set<Permission> permissions) {
        if (managerName == null || managerName.isBlank()) {
            view.showError("Full name is required.");
            return;
        }

        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null || token.isBlank()) {
            view.showError(ExceptionHandlers.getErrorMessage(1001)); // Use predefined error message
            return;
        }

        try {
            // Build the URL with query parameters, URI-encoded
            String url = String.format("http://localhost:8080/api/store/makeOfferManager?storeId=%d&token=%s&managerName=%s",
                    storeId,
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    UriUtils.encodeQueryParam(managerName, StandardCharsets.UTF_8));

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Body: List of Permissions
            HttpEntity<List<Permission>> entity = new HttpEntity<>(List.copyOf(permissions), headers);

            // Request
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ApiResponse.class
            );

            ApiResponse body = response.getBody();
            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                view.showSuccess("✅ Manager offer sent successfully!");
                UI.getCurrent().navigate("manage-store-managers/" + storeId);
            } else {
                if (body != null && body.getErrNumber() != -1) {
                    view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
                } else {
                    view.showError("Failed: " + (body != null ? body.getErrorMsg() : "unknown error"));
                }
            }

        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = new ObjectMapper().readValue(responseBody, ApiResponse.class);

                if (errorBody.getErrNumber() != -1) {
                    view.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    view.showError("❌ FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parseEx) {
                view.showError("❌ HTTP error: " + e.getMessage());
            }
        } catch (Exception e) {
            view.showError("❌ UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    public void navigateBack(String storeId) {
        UI.getCurrent().navigate("manage-store-managers/" + storeId);
    }
}
