package workshop.demo.PresentationLayer.Presenter;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.PresentationLayer.View.MainLayout;
import workshop.demo.PresentationLayer.View.NotificationView;

public class InitPresenter {

    private final RestTemplate restTemplate;
    private final MainLayout view;

    public InitPresenter(MainLayout view) {
        this.view = view;
        this.restTemplate = new RestTemplate();
        createHeader();
        initGuestIfNeeded();

    }

    private void initGuestIfNeeded() {
        Object token = VaadinSession.getCurrent().getAttribute("auth-token");
        Object role = VaadinSession.getCurrent().getAttribute("user-type");
        // If no token exists, this is a first-time guest
        if (token == null) {
            connectAsGuest();
        }
    }

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
                    });
            ApiResponse body = response.getBody();
            if (body != null && body.getErrorMsg() == null) {
                String guestToken = (String) body.getData();

                VaadinSession.getCurrent().setAttribute("auth-token", guestToken);
                VaadinSession.getCurrent().setAttribute("user-type", "guest");

                System.out.println("Guest token stored: " + guestToken);
            } else {
                // view.showError(body.getErrorMsg());
                System.err.println(" Failed to generate guest: "
                        + (body != null ? body.getErrorMsg() : "null body"));
            }
        } catch (Exception e) {
            System.err.println(" Exception during guest generation: " + e.getMessage());
        }
    }

    public void handleOnAttach(String endpoint, Object user) {
        // Log who is currently attached to the UI
        initGuestIfNeeded();
        // connectAsGuest();
        

    }

    private void createHeader() {
        H1 logo = new H1("🛒 Click Market");
        logo.addClassName("market-title");

        Paragraph subtitle = new Paragraph(
                "Welcome to our market. We bring the best stores and products to your fingertips.\n"
                        + "Join us and be an owner of your own store in a few clicks.");
        subtitle.addClassName("market-subtitle");

        VerticalLayout titleLayout = new VerticalLayout(logo, subtitle);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.addClassName("header-title");

        HorizontalLayout header = new HorizontalLayout(titleLayout);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName("app-header");

        Object userType = VaadinSession.getCurrent().getAttribute("user-type");
        boolean isLoggedIn = userType != null && !"guest".equals(userType);

        if (isLoggedIn) {
            Button billButton = new Button("🧾 Notifications");
            billButton.addClickListener(e -> {
                UI.getCurrent().getChildren()
                        .filter(c -> c instanceof NotificationView)
                        .map(c -> (NotificationView) c)
                        .findFirst()
                        .ifPresent(NotificationView::openNotificationBill);
            });

            billButton.getStyle()
                    .set("position", "absolute")
                    .set("bottom", "0")
                    .set("left", "0")
                    .set("margin", "10px");

            header.getElement().getStyle().set("position", "relative"); // anchor container
            header.add(billButton);
        }

        this.view.addToNavbar(header);
    }

}
