package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.SpecialCartItemDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.MyCartView;
import workshop.demo.PresentationLayer.View.NotificationView;
import workshop.demo.PresentationLayer.View.PurchaseView;

public class MyCartPresenter {

    private final RestTemplate restTemplate = new RestTemplate();
    private final MyCartView view;

    public MyCartPresenter(MyCartView view) {
        this.view = view;
    }

    // public void loadCartItems() {
    // loadRegularCartItems();
    // String userType = (String)
    // VaadinSession.getCurrent().getAttribute("user-type");
    // if (userType.equals("user")) {
    // loadSpecialCartItems();
    // }
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
            ResponseEntity<ApiResponse<List<ItemCartDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<List<ItemCartDTO>>>() {
                    });

            ApiResponse<List<ItemCartDTO>> responseBody = response.getBody();

            if (responseBody != null && responseBody.getErrNumber() == -1) {
                List<ItemCartDTO> itemList = responseBody.getData();
                ItemCartDTO[] items = itemList.toArray(new ItemCartDTO[0]);

                view.displayRegularItems(items);
            } else {

                NotificationView.showError(ExceptionHandlers.getErrorMessage(responseBody.getErrNumber()));
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
            ResponseEntity<ApiResponse<List<SpecialCartItemDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<List<SpecialCartItemDTO>>>() {
                    });
            ApiResponse<List<SpecialCartItemDTO>> responseBody = response.getBody();
            if (responseBody != null && responseBody.getErrNumber() == -1) {
                List<SpecialCartItemDTO> itemList = responseBody.getData();
                SpecialCartItemDTO[] items = itemList.toArray(new SpecialCartItemDTO[0]);

                view.displaySpecialItems(items);
            } else {

                NotificationView.showError(ExceptionHandlers.getErrorMessage(responseBody.getErrNumber()));
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

    public void updateQuantity(int productId, int newQuantity) {
        String token = getToken();
        if (token == null) {
            NotificationView.showError("User not authenticated.");
            return;
        }

        try {
            String url = String.format("http://localhost:8080/api/users/ModifyCart?token=%s&productId=%d&quantity=%d",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    productId,
                    newQuantity);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<Boolean>>() {
                    });

            ApiResponse<Boolean> responseBody = response.getBody();

            if (responseBody != null && Boolean.TRUE.equals(responseBody.getData())) {
                NotificationView.showSuccess("Quantity updated successfully.");
                loadRegularCartItems(); // Refresh the cart
            } else {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(responseBody.getErrNumber()));
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void removeFromCart(int productId) {
        String token = getToken();
        if (token == null) {
            NotificationView.showError("User not authenticated.");
            return;
        }

        try {
            String url = String.format("http://localhost:8080/api/users/removeFromCart?token=%s&productId=%d",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    productId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<Boolean>>() {
                    });

            ApiResponse<Boolean> responseBody = response.getBody();

            if (responseBody != null && Boolean.TRUE.equals(responseBody.getData())) {
                NotificationView.showSuccess("Item removed from cart.");
                loadRegularCartItems(); // Refresh the cart
            } else {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(responseBody.getErrNumber()));
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }
}
