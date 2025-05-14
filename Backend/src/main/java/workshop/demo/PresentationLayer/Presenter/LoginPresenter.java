package workshop.demo.PresentationLayer.Presenter;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.Requests.LoginRequest;
import workshop.demo.PresentationLayer.View.LoginView;

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
            view.showError(ExceptionHandlers.getErrorMessage(1001));
            return;
        }

        LoginRequest request = new LoginRequest(guestToken, username, password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                    "http://localhost:8080/api/users/login",
                    entity,
                    ApiResponse.class
            );

            ApiResponse body = response.getBody();
            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                String newUserToken = (String) body.getData();

                VaadinSession.getCurrent().setAttribute("auth-token", newUserToken);
                view.showSuccess("Logged in successfully!");
            } else {
                //here must ask If we alwayes get an UI excpetion and if we get
                //devException what to do??
                if (body.getErrNumber() != -1) {
                    view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
                } else {
                    view.showError("FAILED " + body.getErrorMsg());
                }
            }

        } catch (Exception ex) {
            view.showError("Server error: " + ex.getMessage());
        }
    }
}
