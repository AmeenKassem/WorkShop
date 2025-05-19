package workshop.demo.PresentationLayer.Presenter;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.ManageOwnersView;
import workshop.demo.PresentationLayer.View.NotificationView;

public class ManageOwnersPresenter {

    private final ManageOwnersView view;
    private final RestTemplate restTemplate;

    public ManageOwnersPresenter(ManageOwnersView view) {
        this.view = view;
        this.restTemplate = new RestTemplate();
    }

    public void loadOwners(int storeId) {
        //here must call the function that not implemented yet
        //just to see
        view.buildManageUI(List.of());

    }

    public void deleteOwner(int storeId, int workerId) {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        String url = String.format(
                "http://localhost:8080/deleteOwner?storeId=%d&token=%s&ownerToDelete=%d",
                storeId,
                token,
                workerId
        );

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    ApiResponse.class
            );

            ApiResponse body = response.getBody();
            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                NotificationView.showSuccess("✅ Owner removed.");
                loadOwners(storeId);
            } else if (body != null && body.getErrNumber() != -1) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            }

        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = new ObjectMapper().readValue(responseBody, ApiResponse.class);

                if (errorBody.getErrNumber() != -1) {
                    NotificationView.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    NotificationView.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                NotificationView.showError("HTTP error: " + e.getMessage());
            }

        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());

        }
    }

    public void offerToAddOwner(int storeId, String newOwnerUsername) {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");

        String url = String.format(
                "http://localhost:8080/api/store/makeOfferOwner?storeId=%d&token=%s&newOwner=%s",
                storeId,
                token,
                newOwnerUsername
        );

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ApiResponse.class
            );

            ApiResponse body = response.getBody();
            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                NotificationView.showSuccess("✅ Offer sent to " + newOwnerUsername);
                loadOwners(storeId);
            } else if (body.getErrNumber() != -1) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            }
        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = new ObjectMapper().readValue(responseBody, ApiResponse.class);

                if (errorBody.getErrNumber() != -1) {
                    NotificationView.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    NotificationView.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                NotificationView.showError("HTTP error: " + e.getMessage());
            }

        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());

        }

    }
}
