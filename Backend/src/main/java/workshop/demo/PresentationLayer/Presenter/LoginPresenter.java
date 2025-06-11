package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;

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
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.LoginView;
import workshop.demo.PresentationLayer.View.NotificationView;

@JsModule("./notification.js")
public class LoginPresenter {

    private final LoginView view;
    private final RestTemplate restTemplate = new RestTemplate();

    public LoginPresenter(LoginView view) {
        this.view = view;
    }

    public void login() {
        String username = view.getUsername();
        String password = view.getPassword();

        // Get guest token from session
        String guestToken = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (guestToken == null) {
            NotificationView.showError(ExceptionHandlers.getErrorMessage(1001));
            return;
        }
        // Build the URL with query parameters
        String url = String.format(
                Base.url+"/api/users/login?token=%s&username=%s&password=%s",
                UriUtils.encodeQueryParam(guestToken, StandardCharsets.UTF_8),
                UriUtils.encodeQueryParam(username, StandardCharsets.UTF_8),
                UriUtils.encodeQueryParam(password, StandardCharsets.UTF_8));
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        // ApiResponse body = null;
        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ApiResponse.class);

            ApiResponse body = response.getBody();
            System.out.println("Response body: " + new ObjectMapper().writeValueAsString(body));

            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                String newUserToken = (String) body.getData();

                VaadinSession.getCurrent().setAttribute("auth-token", newUserToken);
                VaadinSession.getCurrent().setAttribute("user-type", "user");
                VaadinSession.getCurrent().setAttribute("username", username);
                NotificationView.showSuccess("Logged in successfully!");
                if (checkIfAdmin(newUserToken)) {
                    System.out.println("adminnnnn");
                    VaadinSession.getCurrent().setAttribute("user-type", "admin");
                    VaadinSession.getCurrent().setAttribute("auth-token", newUserToken);
                    System.out.println("the token:");
                    System.out.println(newUserToken);
                }
                NotificationView notificationView = new NotificationView();
                notificationView.createWS(UI.getCurrent(), username);
                notificationView.register(UI.getCurrent());
                VaadinSession.getCurrent().setAttribute("notification-view", notificationView);

                view.refreshLayoutButtons();
                UI.getCurrent().navigate("");
            } else {
                if (body.getErrNumber() != -1) {
                    //NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
                    view.handleLoginError(body.getErrNumber());
                } else {
                    NotificationView.showError("UnExpected login failure.");
                }
            }

        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = new ObjectMapper().readValue(responseBody, ApiResponse.class);

                if (errorBody.getErrNumber() != -1) {
                    view.handleLoginError(errorBody.getErrNumber());
                    //NotificationView.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    NotificationView.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                NotificationView.showError("HTTP error: " + parsingEx.getMessage());
            }

        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());

        }
    }

    private boolean checkIfAdmin(String token) {
        try {
            System.out.println("in check if admin exception");
            String url = String.format(Base.url+"/api/users/getUserDTO?token=%s",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8));
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
            ApiResponse body = response.getBody();
            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                ObjectMapper mapper = new ObjectMapper();
                UserDTO dto = mapper.convertValue(body.getData(), UserDTO.class);
                System.out.println(dto.getIsAdmin());
                return dto.getIsAdmin();
            }
        } catch (Exception e) {
            System.out.println("in check if admin exception");
            ExceptionHandlers.handleException(e);

        }
        return false;
    }
}
