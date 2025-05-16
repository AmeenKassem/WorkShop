package workshop.demo.PresentationLayer.Presenter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DTOs.ManagerDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.PresentationLayer.View.ManageStoreManagersView;

public class ManageStoreManagersPresenter {

    private final ManageStoreManagersView view;
    private final RestTemplate restTemplate = new RestTemplate();

    public ManageStoreManagersPresenter(ManageStoreManagersView view) {
        this.view = view;
    }

    public void loadManagers(int storeId) {
        String token = getToken();
        if (token == null) {
            view.showError("Token not found in session.");
            return;
        }

        String url = "http://localhost:8080/api/store-managers/getManagers?token=" + token + "&storeId=" + storeId;

        try {
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
            ApiResponse res = response.getBody();

            if (res != null && res.getErrorMsg() == null) {
                ObjectMapper mapper = new ObjectMapper();
                ManagerDTO[] managers = mapper.convertValue(res.getData(), ManagerDTO[].class);
                view.displayManagers(Arrays.asList(managers));
            } else {
                view.showError("Failed to load managers: " + (res != null ? res.getErrorMsg() : "Unknown"));
            }
        } catch (Exception e) {
            view.showError("Server error: " + e.getMessage());
        }
    }

    public void loadOwnedStores() {
        String token = getToken();
        if (token == null) {
            view.showError("Token not found in session.");
            return;
        }

        String url = "http://localhost:8080/api/stores/owned?token=" + token;

        try {
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
            ApiResponse res = response.getBody();

            if (res != null && res.getErrorMsg() == null) {
                ObjectMapper mapper = new ObjectMapper();
                StoreDTO[] stores = mapper.convertValue(res.getData(), StoreDTO[].class);
                Map<String, Integer> storeMap = new HashMap<>();
                for (StoreDTO store  : stores) {
                    storeMap.put(store.getStoreName(), store.getStoreId());
                }

                view.setStores(storeMap);
            } else {
                view.showError("Failed to load store list: " + (res != null ? res.getErrorMsg() : "Unknown"));
            }
        } catch (Exception e) {
            view.showError("Server error: " + e.getMessage());
        }
    }

    public void updatePermissions(ManagerDTO manager) {
        String token = getToken();
        if (token == null) {
            view.showError("Token not found.");
            return;
        }

        String url = "http://localhost:8080/api/store-managers/change-permissions?token=" + token;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ManagerDTO> entity = new HttpEntity<>(manager, headers);

        try {
            ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    ApiResponse.class);

            ApiResponse res = response.getBody();
            if (res != null && res.getErrorMsg() == null) {
                view.showSuccess("Permissions updated.");
            } else {
                view.showError("Error: " + (res != null ? res.getErrorMsg() : "Unknown error"));
            }
        } catch (Exception e) {
            view.showError("Server error: " + e.getMessage());
        }
    }

    public void removeManager(int storeId, int managerId) {
        String token = getToken();
        if (token == null) {
            view.showError("Token not found.");
            return;
        }

        String url = "http://localhost:8080/api/store-managers/remove?token=" + token +
                "&storeId=" + storeId + "&managerId=" + managerId;

        try {
            restTemplate.delete(url);
            view.showSuccess("Manager removed.");
            view.refresh(); 
        } catch (Exception e) {
            view.showError("Failed to remove manager: " + e.getMessage());
        }
    }

    private String getToken() {
        return (String) VaadinSession.getCurrent().getAttribute("auth-token");
    }
}
