package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

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
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.MyStoresView;
import workshop.demo.PresentationLayer.View.NotificationView;

public class MyStoresPresenter {

    private final RestTemplate restTemplate;
    private final MyStoresView view;

    public MyStoresPresenter(MyStoresView view) {
        this.view = view;
        restTemplate = new RestTemplate();
    }

    public void loadMyStores() {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null || token.isBlank()) {
            view.showError("You must be logged in to view your stores.");
            UI.getCurrent().navigate(""); // navigate to home
            return;
        }
        String url = String.format("http://localhost:8080/api/store/myStores?token=%s",
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

            ApiResponse body = response.getBody();
            System.out.println("Response: " + new ObjectMapper().writeValueAsString(body));

            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                ObjectMapper mapper = new ObjectMapper();
                List<?> rawList = (List<?>) body.getData();

                List<StoreDTO> stores = rawList.stream()
                        .map(obj -> mapper.convertValue(obj, StoreDTO.class))
                        .collect(Collectors.toList());

                view.displayStores(stores);

            } else if (body != null && body.getErrNumber() != -1) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
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

        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());
        }

    }

}
