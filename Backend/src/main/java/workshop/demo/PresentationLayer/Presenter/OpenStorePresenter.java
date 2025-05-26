package workshop.demo.PresentationLayer.Presenter;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.OpenStoreView;

public class OpenStorePresenter {

    private final OpenStoreView view;
    private final RestTemplate restTemplate;

    public OpenStorePresenter(OpenStoreView view) {
        this.view = view;
        restTemplate = new RestTemplate();
    }

    public void openStore() {
        String storeName = view.getStoreName();
        String category = view.getCategory();
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        String url = String.format(
                "http://localhost:8080/api/store/addStore?token=%s&storeName=%s&category=%s",
                token,
                storeName,
                category
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ApiResponse.class
            );

            ApiResponse body = response.getBody();
            System.out.println("Response: " + new ObjectMapper().writeValueAsString(body));

            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                view.showSuccess("Store created successfully!");
                UI.getCurrent().navigate("");
            } else if (body != null && body.getErrNumber() != -1) {
                view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            }

        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = new ObjectMapper().readValue(responseBody, ApiResponse.class);
                if (errorBody.getErrNumber() != -1) {
                    view.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    view.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                view.showError("HTTP error: " + e.getMessage());
            }

        } catch (Exception e) {
            view.showError("UNEXPECTED ERROR: " + e.getMessage());
        }

    }

}
