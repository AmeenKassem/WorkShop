package workshop.demo.PresentationLayer.Presenter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.HomePage;

public class HomePagePresenter {

    private final RestTemplate restTemplate;
    private HomePage view;

    public HomePagePresenter(HomePage homePage) {
        this.view = homePage;
        this.restTemplate = new RestTemplate();
    }

    public List<StoreDTO> fetchStores() {
        try {
            ResponseEntity<StoreDTO[]> response = restTemplate.getForEntity(
                    "http://localhost:8080/api/stores/allStores", StoreDTO[].class);
            StoreDTO[] body = response.getBody();
            return body != null ? Arrays.asList(body) : Collections.emptyList();
        } catch (Exception e) {
            Notification.show("❌ Failed to load stores: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Div createStoreCard(StoreDTO store) {
        Div card = new Div();
        card.addClassName("store-card");

        H3 name = new H3(store.getName());
        Paragraph rank = new Paragraph("⭐ Rank: " + store.getRank());
        Paragraph category = new Paragraph("🏷️ Category: " + store.getCategory());

        card.add(name, rank, category);

        card.addClickListener(e
                -> UI.getCurrent().navigate("store/" + store.getId()));

        return card;
    }

    public void handleLogout() {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        String type = (String) VaadinSession.getCurrent().getAttribute("user-type");
        System.out.println("in logout -> presenter");
        System.out.println("token; " + token);
        System.out.println("the user type is: " + type);

        if (token != null) {
            System.out.println("the user type is: " + type);
            System.out.println("the token is: " + token);
            try {
                ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                        "http://localhost:8080/api/users/logout?token=" + token,
                        null,
                        ApiResponse.class
                );

                ApiResponse body = response.getBody();
                if (body != null && body.getErrNumber() != -1) {
                    view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));

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

        // Clear session and redirect
        //guest token -> main layout it wil generated aoyomaticlly
        VaadinSession.getCurrent().setAttribute("auth-token", null);
        VaadinSession.getCurrent().setAttribute("user-type", "guest");
        //UI.getCurrent().navigate("");
        UI.getCurrent().getPage().reload(); // Force hard refresh to reinitialize MainLayout
    }

}
