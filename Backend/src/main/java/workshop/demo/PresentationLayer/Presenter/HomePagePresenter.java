package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.HomePage;
import workshop.demo.PresentationLayer.View.NotificationView;

public class HomePagePresenter {

    private RestTemplate restTemplate;
    private HomePage view;
    private final String BASE_URL = Base.url+"/stock";

    public HomePagePresenter(HomePage homePage) {
        this.view = homePage;
        this.restTemplate = new RestTemplate();
    }

    public List<StoreDTO> fetchStores() {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        try {
            String url = String.format(
                    Base.url+"/api/store/allStores?token=%s",
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

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
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

    public List<ItemStoreDTO> searchNormal(String token, String name, String keyword, Category category,
            Double minPrice, Double maxPrice, Integer productRate) {
        try {
            String url = buildUrl("/searchProducts", token, name, keyword, category, minPrice, maxPrice, productRate);
            ResponseEntity<ApiResponse<ItemStoreDTO[]>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(buildHeaders()),
                    new ParameterizedTypeReference<>() {
            }
            );
            ApiResponse<ItemStoreDTO[]> body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                return List.of(body.getData());
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
        return Collections.emptyList();
    }

    private String buildUrl(String path, String token, String name, String keyword, Category category,
            Double minPrice, Double maxPrice, Integer productRate) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL + path)
                .queryParam("token", token);

        if (name != null && !name.isEmpty()) {
            builder.queryParam("productNameFilter", name);
        }
        if (keyword != null && !keyword.isEmpty()) {
            builder.queryParam("keywordFilter", keyword);
        }
        if (category != null) {
            builder.queryParam("categoryFilter", category);
        }
        if (minPrice != null) {
            builder.queryParam("minPrice", minPrice);
        }
        if (maxPrice != null) {
            builder.queryParam("maxPrice", maxPrice);
        }
        if (productRate != null) {
            builder.queryParam("minProductRating", productRate);
        }

        return builder.toUriString();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    public void addToCart(String token, ItemStoreDTO item, int quantity) {
        Map<String, Object> body = new HashMap<>();
        body.put("item", item);
        body.put("quantity", quantity);
        String url = String.format(
                Base.url+"/api/users/addToCart?token=%s",
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<?> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ApiResponse.class
            );

            ApiResponse bodyResponse = response.getBody();
            if (bodyResponse != null && bodyResponse.getErrNumber() == -1) {
                NotificationView.showSuccess("Added to cart successfully.");
            } else if (bodyResponse != null) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(bodyResponse.getErrNumber()));
            } else {
                NotificationView.showError("Unexpected empty response.");
            }

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }
}
