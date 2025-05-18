package workshop.demo.PresentationLayer.Presenter;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.ManageStoreView;

public class ManageStorePresenter {

    private final ManageStoreView view;
    private final RestTemplate restTemplate;

    public ManageStorePresenter(ManageStoreView view) {
        this.view = view;
        this.restTemplate = new RestTemplate();
    }

    public void fetchStore(String token, int storeId) {
        String url = String.format(
                "http://localhost:8080/api/store/getstoreDTO?token=%s&storeId=%d",
                token,
                storeId
        );

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
                StoreDTO store = mapper.convertValue(body.getData(), StoreDTO.class);
                view.buildManageUI(store);
            } else if (body != null && body.getErrNumber() != -1) {
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

}
