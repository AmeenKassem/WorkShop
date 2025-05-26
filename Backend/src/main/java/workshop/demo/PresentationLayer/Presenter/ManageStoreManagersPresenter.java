package workshop.demo.PresentationLayer.Presenter;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.ManageStoreManagersView;
import workshop.demo.PresentationLayer.View.NotificationView;

public class ManageStoreManagersPresenter {

    private final ManageStoreManagersView view;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String storeId;

    public ManageStoreManagersPresenter(ManageStoreManagersView view) {
        this.view = view;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
        loadManagers();
    }

    public void loadManagers() {
        try {
            String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
            URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/store/viewRolesAndPermissions")
                    .queryParam("token", token)
                    .queryParam("storeId", storeId)
                    .build().toUri();

            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(uri, ApiResponse.class);
            ApiResponse body = response.getBody();

            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                List<WorkerDTO> managers = objectMapper.convertValue(body.getData(), new TypeReference<>() {
                });
                view.updateManagerList(managers);
            } else if (body != null) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                NotificationView.showError("Unexpected error: Empty response");
            }

        } catch (HttpClientErrorException e) {
            handleHttpClientException(e);
        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    public void addManager(String username, Map<Permission, Checkbox> checkboxMap) {
        try {
            Set<Permission> selectedPermissions = checkboxMap.entrySet().stream()
                    .filter(e -> e.getValue().getValue())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
            URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/store/makeOfferManager")
                    .queryParam("storeId", storeId)
                    .queryParam("token", token)
                    .queryParam("managerName", username)
                    .build().toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Set<Permission>> entity = new HttpEntity<>(selectedPermissions, headers);

            ResponseEntity<ApiResponse> response = restTemplate.exchange(uri, HttpMethod.POST, entity, ApiResponse.class);
            ApiResponse body = response.getBody();

            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                view.showSuccess("Manager offer sent successfully");
                loadManagers();
            } else if (body != null) {
                view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                view.showError("Unexpected error: Empty response");
            }

        } catch (HttpClientErrorException e) {
            handleHttpClientException(e);
        } catch (Exception e) {
            view.showError("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    public void savePermissions(int managerId, Map<Permission, Checkbox> checkboxMap) {
        try {
            Set<Permission> selected = checkboxMap.entrySet().stream()
                    .filter(e -> e.getValue().getValue())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");

            URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/store/changePermissions")
                    .queryParam("token", token)
                    .queryParam("managerId", managerId)
                    .queryParam("storeId", storeId)
                    .build().toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Set<Permission>> entity = new HttpEntity<>(selected, headers);

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
            handleHttpClientException(e);
        } catch (Exception e) {
            view.showError("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    public void deleteManager(int managerId) {
        try {
            String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");

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
                loadManagers();
            } else if (body != null) {
                view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                view.showError("Unexpected error: Empty response");
            }
        } catch (HttpClientErrorException e) {
            handleHttpClientException(e);
        } catch (Exception e) {
            view.showError("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    private void handleHttpClientException(HttpClientErrorException e) {
        try {
            String responseBody = e.getResponseBodyAsString();
            ApiResponse errorBody = objectMapper.readValue(responseBody, ApiResponse.class);
            view.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
        } catch (Exception parsingEx) {
            view.showError("HTTP error: " + e.getMessage());
        }
    }
}
