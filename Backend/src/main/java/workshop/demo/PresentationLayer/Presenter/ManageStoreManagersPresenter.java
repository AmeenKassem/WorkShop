package workshop.demo.PresentationLayer.Presenter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.vaadin.flow.server.VaadinSession;
import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DTOs.ManagerDTO;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.ManageStoreManagersView;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class ManageStoreManagersPresenter {

    private final ManageStoreManagersView view;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ManageStoreManagersPresenter(ManageStoreManagersView view) {
        this.view = view;
    }

    public void loadManagers(String storeId) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/store/viewRoles")
                    .queryParam("storeId", storeId)
                    .build().toUri();

            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(uri, ApiResponse.class);
            ApiResponse body = response.getBody();

            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                List<ManagerDTO> managers = objectMapper.convertValue(body.getData(), new TypeReference<>() {});
                view.updateManagerList(managers);
            } else if (body != null) {
                view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                view.showError("Unexpected error: Empty response");
            }
        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = objectMapper.readValue(responseBody, ApiResponse.class);
                view.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
            } catch (Exception parsingEx) {
                view.showError("HTTP error: " + e.getMessage());
            }
        } catch (Exception e) {
            view.showError("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    public void updateManagerPermissions(String token, String storeId, int managerId, Set<Permission> permissions) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/store/changePermissions")
                    .queryParam("token", token)
                    .queryParam("managerId", managerId)
                    .queryParam("storeId", storeId)
                    .build().toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Set<Permission>> entity = new HttpEntity<>(permissions, headers);

            ResponseEntity<ApiResponse> response = restTemplate.exchange(uri, HttpMethod.POST, entity, ApiResponse.class);
            ApiResponse body = response.getBody();

            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                view.showSuccess("Permissions updated");
            } else if (body != null) {
                view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                view.showError("Unexpected error: Empty response");
            }

        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = objectMapper.readValue(responseBody, ApiResponse.class);
                view.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
            } catch (Exception parsingEx) {
                view.showError("HTTP error: " + e.getMessage());
            }
        } catch (Exception e) {
            view.showError("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    public void removeManager(String token, String storeId, int managerId) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/store/deleteManager")
                    .queryParam("token", token)
                    .queryParam("managerId", managerId)
                    .queryParam("storeId", storeId)
                    .build().toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse> response = restTemplate.exchange(uri, HttpMethod.POST, entity, ApiResponse.class);
            ApiResponse body = response.getBody();

            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                view.showSuccess("Manager deleted");
                loadManagers(storeId); // Refresh
            } else if (body != null) {
                view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                view.showError("Unexpected error: Empty response");
            }

        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = objectMapper.readValue(responseBody, ApiResponse.class);
                view.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
            } catch (Exception parsingEx) {
                view.showError("HTTP error: " + e.getMessage());
            }
        } catch (Exception e) {
            view.showError("UNEXPECTED ERROR: " + e.getMessage());
        }
    }
}
