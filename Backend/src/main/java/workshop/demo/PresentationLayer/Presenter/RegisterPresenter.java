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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.NotificationView;
import workshop.demo.PresentationLayer.View.RegisterView;

public class RegisterPresenter {

    private final RegisterView view;
    private final RestTemplate restTemplate = new RestTemplate();

    public RegisterPresenter(RegisterView view) {
        this.view = view;
    }

    public void register() {
        String username = view.getUsername();
        String password = view.getPassword();
        int age = view.getAge();
        if (!view.isPasswordValid(password)) {
            NotificationView.showError(" Password is too weak.");
            return;
        }

        if (age <= 0) {
            NotificationView.showError("Invalid age. Please enter a positive number.");
            return;
        }

        String guestToken = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (guestToken == null) {
            NotificationView.showError(ExceptionHandlers.getErrorMessage(1001));
            return;
        }

        String url = String.format(
                "http://localhost:8080/api/users/register?token=%s&username=%s&password=%s&age=%d",
                UriUtils.encodeQueryParam(guestToken, StandardCharsets.UTF_8),
                UriUtils.encodeQueryParam(username, StandardCharsets.UTF_8),
                UriUtils.encodeQueryParam(password, StandardCharsets.UTF_8),
                age
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
            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                Boolean success = (Boolean) body.getData();
                if (Boolean.TRUE.equals(success)) {

                    NotificationView.showSuccess("Registered successfully!");
                    UI.getCurrent().navigate("login"); // go to login page
                } else {
                    NotificationView.showError("Registration failed.");
                }

            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }
}
