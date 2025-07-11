package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DTOs.UserSuspensionDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.AdminPageView;
import workshop.demo.PresentationLayer.View.NotificationView;

public class AdminPagePresenter {

    private final RestTemplate restTemplate;
    private final AdminPageView view;

    public AdminPagePresenter(AdminPageView view) {
        this.restTemplate = new RestTemplate();
        this.view = view;

    }

    public List<UserDTO> getAllUsers() throws UIException {
        Object token = VaadinSession.getCurrent().getAttribute("auth-token");
        String url = Base.url + "/api/users/getAllUsers?token=" + token;

        try {
            ResponseEntity<ApiResponse<List<UserDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
            }
            );

            ApiResponse<List<UserDTO>> apiResponse = response.getBody();

            if (apiResponse != null && apiResponse.getErrorMsg() == null) {
                return apiResponse.getData();
            } else {
                throw new UIException(apiResponse.getErrorMsg(), apiResponse.getErrNumber());
            }

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
        return new ArrayList<>();
    }

    public void onSuspendUser(int userId, int minutes) {
        Object token = VaadinSession.getCurrent().getAttribute("auth-token");
        String url = String.format(Base.url + "/api/users/suspendUser?userId=%d&minutes=%d&token=%s",
                userId, minutes, token);

        try {
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Boolean>>() {
            }
            );

            ApiResponse<Boolean> res = response.getBody();

            if (res != null && res.getErrorMsg() == null) {
                NotificationView.showSuccess(" User suspended successfully.");
            }

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void onPauseSuspension(int userId) {
        Object token = VaadinSession.getCurrent().getAttribute("auth-token");
        String url = String.format(Base.url + "/api/users/pauseSuspension?userId=%d&token=%s",
                userId, token);

        try {
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Boolean>>() {
            }
            );

            ApiResponse<Boolean> res = response.getBody();

            if (res != null && res.getErrorMsg() == null) {
                NotificationView.showSuccess("Suspension paused");
            }

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void onResumeSuspension(int userId) {
        Object token = VaadinSession.getCurrent().getAttribute("auth-token");
        String url = String.format(Base.url + "/api/users/resumeSuspension?userId=%d&token=%s",
                userId, token);

        try {
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Boolean>>() {
            }
            );

            ApiResponse<Boolean> res = response.getBody();

            if (res != null && res.getErrorMsg() == null) {
                NotificationView.showSuccess("Suspension resumed");
            }

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void onCancelSuspension(int userId) {
        Object token = VaadinSession.getCurrent().getAttribute("auth-token");
        String url = String.format(Base.url + "/api/users/cancelSuspension?userId=%d&token=%s",
                userId, token);

        try {
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Boolean>>() {
            }
            );

            ApiResponse<Boolean> res = response.getBody();

            if (res != null && res.getErrorMsg() == null) {
                NotificationView.showSuccess("Suspension cancelled successfully.");
            } else if (res != null) {
                NotificationView.showError("Error: " + res.getErrorMsg());
            }

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void onRemoveUser(UserDTO user) {
        Object token = VaadinSession.getCurrent().getAttribute("auth-token");
        NotificationView.showError("coming soon!");
        // TODO: logic to remove the user
    }

    public List<StoreDTO> getAllStores() {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        try {
            String url = String.format(
                    Base.url + "/api/store/allStores?token=%s",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<List<StoreDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<List<StoreDTO>>>() {
            });

            ApiResponse<List<StoreDTO>> responseBody = response.getBody();

            if (responseBody != null && responseBody.getErrNumber() == -1) {
                System.out.println("int stores -> not empty");
                return responseBody.getData() != null ? responseBody.getData() : Collections.emptyList();
            }
            return Collections.emptyList();
        } catch (Exception e) {

            ExceptionHandlers.handleException(e);
            return Collections.emptyList();
        }
    }

    public void onCloseStore(int storeId) {
        String token = String.valueOf(VaadinSession.getCurrent().getAttribute("auth-token"));
        String url = String.format(Base.url + "/api/store/close?storeId=%d&token=%s", storeId, token);

        try {
            ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<ApiResponse<String>>() {
            }
            );

            ApiResponse<String> res = response.getBody();

            if (res != null && res.getErrorMsg() == null) {
                NotificationView.showSuccess("Store closed successfully!");
            }

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public List<UserSuspensionDTO> onViewSuspensions() {
        Object token = VaadinSession.getCurrent().getAttribute("auth-token");
        String url = String.format(Base.url + "/api/users/viewSuspensions?token=%s", token);

        try {
            ResponseEntity<ApiResponse<List<UserSuspensionDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<List<UserSuspensionDTO>>>() {
            }
            );

            ApiResponse<List<UserSuspensionDTO>> res = response.getBody();

            if (res != null && res.getErrorMsg() == null) {
                return res.getData();
            } else if (res != null) {
                NotificationView.showError("Error: " + res.getErrorMsg());
            }

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }

        return Collections.emptyList();
    }

    public boolean shutdownSystem(int key) {
        String url = String.format(Base.url + "/api/appsettings/shutdown?key=%d", key);

        try {
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Boolean>>() {
            }
            );
            ApiResponse<Boolean> body = response.getBody();

            if (body != null && body.getErrorMsg() == null && body.getData() != null) {
                return body.getData();
            } else {
                NotificationView.showError(body != null && body.getErrorMsg() != null
                        ? body.getErrorMsg()
                        : "Shutdown request failed.");
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }

        return false;

    }
}
