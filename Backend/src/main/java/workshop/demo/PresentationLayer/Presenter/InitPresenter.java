package workshop.demo.PresentationLayer.Presenter;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.PresentationLayer.View.LoginView;
import workshop.demo.PresentationLayer.View.MainLayout;
import workshop.demo.PresentationLayer.View.RegisterView;

public class initPresenter {

    private final RestTemplate restTemplate;
    private final MainLayout view;

    public initPresenter(MainLayout view) {
        this.view = view;
        this.restTemplate = new RestTemplate();
        createHeader();
        //initGuestIfNeeded();

    }

    // private void initGuestIfNeeded() {
    //     Object token = VaadinSession.getCurrent().getAttribute("auth-token");
    //     Object role = VaadinSession.getCurrent().getAttribute("auth-role");
    //     // If no token exists, this is a first-time guest
    //     if (token == null || !"guest".equals(role)) {
    //         connectAsGuest();
    //     }
    // }
    private void connectAsGuest() {
        try {
            String url = "http://localhost:8080/api/users/generateGuest";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // Optional for GET/params
            HttpEntity<Void> entity = new HttpEntity<>(headers); // no body
            ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<String>>() {
            }
            );
            ApiResponse body = response.getBody();
            if (body != null && body.getErrorMsg() == null) {
                String guestToken = (String) body.getData();

                VaadinSession.getCurrent().setAttribute("auth-token", guestToken);
                VaadinSession.getCurrent().setAttribute("auth-role", "guest");

                System.out.println("Guest token stored: " + guestToken);
            } else {
                //view.showError(body.getErrorMsg());
                System.err.println(" Failed to generate guest: "
                        + (body != null ? body.getErrorMsg() : "null body"));
            }
        } catch (Exception e) {
            System.err.println(" Exception during guest generation: " + e.getMessage());
        }
    }

    public void handleOnAttach(String endpoint, Object user) {
        // Log who is currently attached to the UI
        connectAsGuest();

        // System.out.println("📌 MainLayout attached");
        // System.out.println("   ➤ Role: " + role);
        // System.out.println("   ➤ Token: " + token);
        // If you plan to use a WebSocket notification system later, this is where you'd connect:
        // notificationHandler.connect(endpoint, user.toString());
        // Optional: redirect logic
        // if ("guest".equals(role)) {
        //     UI.getCurrent().navigate("login"); // or show a banner
        // }
    }

    private void createHeader() {
        // H1 logo = new H1("🛒 Click Market");
        // logo.addClassName("market-title");
        // Paragraph subtitle = new Paragraph(
        //         "Welcome to our market. We bring the best stores and products to your fingertips.\n"
        //         + "Join us and be an owner of your own store in a few clicks."
        // );
        // subtitle.addClassName("market-subtitle");

        // RouterLink login = new RouterLink("Login", LoginView.class);
        // RouterLink register = new RouterLink("Register", RegisterView.class);
        // RouterLink MyCart = new RouterLink("My Cart", LoginView.class);
        // login.addClassName("header-button");
        // register.addClassName("header-button");
        // MyCart.addClassName("header-button");
        // HorizontalLayout buttonLayout = new HorizontalLayout(login, register, MyCart);
        // buttonLayout.addClassName("header-left");
        // HorizontalLayout header = new HorizontalLayout(buttonLayout);
        // header.setWidthFull();
        // header.setAlignItems(FlexComponent.Alignment.CENTER);
        // header.addClassName("app-header");
        // // Add both title and button layout
        // header.add(logo, buttonLayout, subtitle);
        // this.view.addToNavbar(header);
        // Title and subtitle
        H1 logo = new H1("🛒 Click Market");
        logo.addClassName("market-title");

        Paragraph subtitle = new Paragraph(
                "Welcome to our market. We bring the best stores and products to your fingertips.\n"
                + "Join us and be an owner of your own store in a few clicks."
        );
        subtitle.addClassName("market-subtitle");

        VerticalLayout titleLayout = new VerticalLayout(logo, subtitle);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.addClassName("header-title");

        HorizontalLayout header = new HorizontalLayout(titleLayout);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName("app-header");

        this.view.addToNavbar(header);
    }

}
