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
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
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
            view.showError(ExceptionHandlers.getErrorMessage(1001));
            return;
        }
        // Build the URL with query parameters
        String url = String.format(
                "http://localhost:8080/api/users/login?token=%s&username=%s&password=%s",
                UriUtils.encodeQueryParam(guestToken, StandardCharsets.UTF_8),
                UriUtils.encodeQueryParam(username, StandardCharsets.UTF_8),
                UriUtils.encodeQueryParam(password, StandardCharsets.UTF_8));
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        System.out.println("Guest token: " + guestToken);
        System.out.println("Final URL: " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // Optional here, since no body is sent

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
                view.showSuccess("Logged in successfully!");

                // Initialize WebSocket connection
                VaadinSession session = VaadinSession.getCurrent();
                session.setAttribute("username", username);

                try {
                    NotificationView.create().register(UI.getCurrent());
                } catch (Exception e) {
                    System.err.println("Error registering NotificationView: " + e.getMessage());
                }

                if (username != null) {
                    // Add a small delay to ensure JS is fully loaded
                    UI ui = UI.getCurrent();
                    if (ui != null) {
                        Page page = ui.getPage();

                        // First check if the notification.js is loaded
                        page.executeJs("console.log('Testing if JS module is loaded properly');");

                        // Then initialize the WebSocket with proper error handling
                        page.executeJs("try { " +
                                "console.log('About to initialize notification socket for: ' + $0); " +
                                "if (typeof window.initNotificationSocket === 'function') { " +
                                "  window.initNotificationSocket($0); " +
                                "  console.log('WebSocket initialization called'); " +
                                "} else { " +
                                "  console.error('initNotificationSocket function not found!'); " +
                                "} " +
                                "} catch(e) { console.error('Error initializing WebSocket:', e); }", username);

                        // Set the flag to avoid multiple initializations
                        session.setAttribute("ws-initialized", true);
                        // Register the notification view to this UI
                        // UI.getCurrent().addAttachListener(event -> {
                        // NotificationView.register(event.getUI());

                        // });
                        System.out.println("WebSocket initialization attempted for user: " + username);
                    }
                } else {
                    System.out.println("Cannot initialize WebSocket: username is null");
                }
                // ------------------------------->

                UI.getCurrent().navigate("");
            } else {
                if (body.getErrNumber() != -1) {
                    view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
                }
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
