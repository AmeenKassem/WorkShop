package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.HomePage;
import workshop.demo.PresentationLayer.View.NotificationView;

public class HomePagePresenter {

    private final RestTemplate restTemplate;
    private HomePage view;

    public HomePagePresenter(HomePage homePage) {
        this.view = homePage;
        this.restTemplate = new RestTemplate();
    }

    public List<StoreDTO> fetchStores() {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        try {
            String url = String.format(
                    "http://localhost:8080/api/store/allStores?token=%s",
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
                return responseBody.getData() != null ? responseBody.getData() : Collections.emptyList();
            } else {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(responseBody.getErrNumber()));
                return Collections.emptyList();
            }

        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = new ObjectMapper().readValue(responseBody, ApiResponse.class);

                if (errorBody.getErrNumber() != -1) {
                    NotificationView.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    NotificationView.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                NotificationView.showError("HTTP error: " + e.getMessage());
            }
            return null;
        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());
            return null;
        }
    }

    public Div createStoreCard(StoreDTO store) {
        Div card = new Div();
        card.addClassName("store-card");

        H3 name = new H3(store.storeName);
        int filledStars = store.finalRating;
        int emptyStars = 5 - filledStars;
        String stars = "⭐".repeat(filledStars) + "☆".repeat(emptyStars);

        Paragraph rank = new Paragraph("⭐ Rank: " + stars);
        Paragraph category = new Paragraph("🏷️ Category: " + store.category);
        card.add(name, rank, category);
        card.addClickListener(e
                -> UI.getCurrent().navigate("store/" + store.storeId));

        return card;
    }

}
