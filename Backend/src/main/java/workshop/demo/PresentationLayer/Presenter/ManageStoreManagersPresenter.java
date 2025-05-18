package workshop.demo.PresentationLayer.Presenter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import workshop.demo.DTOs.ManagerDTO;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.PresentationLayer.View.ManageStoreManagersView;

public class ManageStoreManagersPresenter {

    private final ManageStoreManagersView view;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "http://localhost:8080/api/store-managers";

    public ManageStoreManagersPresenter(ManageStoreManagersView view) {
        this.view = view;
    }

    public void loadStores() {
        try {
            String token = getToken();
            String url = baseUrl + "/get-owned-stores?token=" + token;
            ResponseEntity<String[]> response = restTemplate.getForEntity(url, String[].class);

            if (response.getStatusCode().is2xxSuccessful()) {
                view.populateStores(Arrays.asList(Objects.requireNonNull(response.getBody())));
            } else {
                view.showError("Failed to load stores.");
            }
        } catch (Exception e) {
            view.showError("Error loading stores: " + e.getMessage());
        }
    }

    public void loadManagers(String storeId) {
        try {
            String token = getToken();
            String url = baseUrl + "/get-store-managers?token=" + token + "&storeId=" + storeId;
            ResponseEntity<ManagerDTO[]> response = restTemplate.getForEntity(url, ManagerDTO[].class);

            if (response.getStatusCode().is2xxSuccessful()) {
                view.updateManagerGrid(Arrays.asList(Objects.requireNonNull(response.getBody())));
            } else {
                view.showError("Failed to fetch managers.");
            }
        } catch (Exception e) {
            view.showError("Error loading managers: " + e.getMessage());
        }
    }

    public void updateManagerPermissions(String token, String storeId, int userId, Set<Permission> permissions) {
        try {
            String url = baseUrl + "/update-manager-permissions?token=" + token + "&storeId=" + storeId + "&userId=" + userId;
            restTemplate.put(url, permissions);
            view.showSuccess("Permissions updated.");
            loadManagers(storeId);
        } catch (Exception e) {
            view.showError("Error updating permissions: " + e.getMessage());
        }
    }

    public void removeManager(String token, String storeId, int userId) {
        try {
            String url = baseUrl + "/remove-manager?token=" + token + "&storeId=" + storeId + "&userId=" + userId;
            restTemplate.delete(url);
            view.showSuccess("Manager removed.");
            loadManagers(storeId);
        } catch (Exception e) {
            view.showError("Error removing manager: " + e.getMessage());
        }
    }

    private String getToken() {
        return (String) com.vaadin.flow.server.VaadinSession.getCurrent().getAttribute("auth-token");
    }
}
