package workshop.demo.PresentationLayer.Presenter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.PresentationLayer.View.MainLayout;

public class InitPresenter {

    private final RestTemplate restTemplate;
    private final MainLayout view;

    public InitPresenter(MainLayout view) {
        this.view = view;
        this.restTemplate = new RestTemplate();
        initGuestIfNeeded();
    }

    private void initGuestIfNeeded() {
        Object token = VaadinSession.getCurrent().getAttribute("auth-token");
        Object role = VaadinSession.getCurrent().getAttribute("auth-role");

        // If no token exists, this is a first-time guest
        if (token == null || !"guest".equals(role)) {
            connectAsGuest();
        }
    }

    private void connectAsGuest() {
        try {
            String url = "http://localhost:8080/api/users/generateGuest";

            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);

            ApiResponse body = response.getBody();
            if (body != null && body.getErrorMsg() == null) {
                String guestToken = (String) body.getData();

                VaadinSession.getCurrent().setAttribute("auth-token", guestToken);
                VaadinSession.getCurrent().setAttribute("auth-role", "guest");

                System.out.println("Guest token stored: " + guestToken);
            } else {
                System.err.println(" Failed to generate guest: "
                        + (body != null ? body.getErrorMsg() : "null body"));
            }
        } catch (Exception e) {
            System.err.println(" Exception during guest generation: " + e.getMessage());
        }
    }

    public void handleOnAttach(String endpoint, Object user) {
        // Log who is currently attached to the UI
        String role = (String) VaadinSession.getCurrent().getAttribute("auth-role");
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");

        System.out.println("📌 MainLayout attached");
        System.out.println("   ➤ Role: " + role);
        System.out.println("   ➤ Token: " + token);

        // If you plan to use a WebSocket notification system later, this is where you'd connect:
        // notificationHandler.connect(endpoint, user.toString());
        // Optional: redirect logic
        // if ("guest".equals(role)) {
        //     UI.getCurrent().navigate("login"); // or show a banner
        // }
    }

}
