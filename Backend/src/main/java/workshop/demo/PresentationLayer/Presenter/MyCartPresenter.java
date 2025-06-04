package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.SpecialCartItemDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.MyCartView;
import workshop.demo.PresentationLayer.View.NotificationView;

public class MyCartPresenter {

    private final RestTemplate restTemplate = new RestTemplate();
    private final MyCartView view;

    public MyCartPresenter(MyCartView view) {
        this.view = view;
    }

    // public void loadCartItems() {
    //     loadRegularCartItems();
    //     String userType = (String) VaadinSession.getCurrent().getAttribute("user-type");
    //     if (userType.equals("user")) {
    //         loadSpecialCartItems();
    //     }
    // }

    public void loadRegularCartItems() {
        String token = getToken();
        if (token == null) {
            return;
        }

        String url = String.format("http://localhost:8080/api/users/getregularcart?token=%s",
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ApiResponse.class
            );

            ApiResponse rawResponse = response.getBody();
            if (rawResponse != null && rawResponse.getErrorMsg() == null) {
                ObjectMapper mapper = new ObjectMapper();
                List<?> rawList = (List<?>) rawResponse.getData();
                List<ItemCartDTO> items = rawList.stream()
                        .map(obj -> mapper.convertValue(obj, ItemCartDTO.class))
                        .collect(Collectors.toList());
                view.displayRegularItems(items.toArray(new ItemCartDTO[0]));
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void loadSpecialCartItems() {
        String token = getToken();
        if (token == null) {
            return;
        }

        String url = String.format("http://localhost:8080/api/users/getspecialcart?token=%s",
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ApiResponse.class
            );

            ApiResponse rawResponse = response.getBody();
            if (rawResponse != null && rawResponse.getErrorMsg() == null) {
                ObjectMapper mapper = new ObjectMapper();
                List<?> rawList = (List<?>) rawResponse.getData();
                List<SpecialCartItemDTO> items = rawList.stream()
                        .map(obj -> mapper.convertValue(obj, SpecialCartItemDTO.class))
                        .collect(Collectors.toList());
                view.displaySpecialItems(items.toArray(new SpecialCartItemDTO[0]));
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    private String getToken() {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null || token.isBlank()) {
            NotificationView.showError("You must be logged in to view your cart.");
            UI.getCurrent().navigate("");
            return null;
        }
        return token;
    }

}
