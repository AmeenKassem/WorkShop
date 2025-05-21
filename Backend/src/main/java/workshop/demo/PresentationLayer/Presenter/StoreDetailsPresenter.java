package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ReviewDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.NotificationView;

public class StoreDetailsPresenter {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public StoreDetailsPresenter() {
        this.restTemplate = new RestTemplate();
        this.mapper = new ObjectMapper();
    }

    public ItemStoreDTO[] getProductsInStore(int storeId) throws Exception {
        String url = "http://localhost:8080/stock/getProductsInStore?storeId=" + storeId;
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
        ApiResponse<?> body = response.getBody();

        if (body == null || body.getErrorMsg() != null || body.getData() == null) {
            throw new Exception(body != null ? body.getErrorMsg() : "Failed to load products");
        }

        return mapper.convertValue(body.getData(), ItemStoreDTO[].class);
    }

    public void addReviewToItem(String token, int storeId, int productId, String review) {
        String url = String.format(
                "http://localhost:8080/api/Review/addToProduct?token=%s&storeId=%d&productId=%d&review=%s",
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                storeId,
                productId,
                UriUtils.encodeQueryParam(review, StandardCharsets.UTF_8)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ApiResponse.class
            );

            ApiResponse body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                NotificationView.showSuccess("Review added successfully.");
            } else if (body != null) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                NotificationView.showError("Unknown error adding review.");
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

    public void addReviewToStore(String token, int storeId, String review) {
        String url = String.format(
                "http://localhost:8080/api/Review/addToStore?token=%s&storeId=%d&review=%s",
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                storeId,
                UriUtils.encodeQueryParam(review, StandardCharsets.UTF_8)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ApiResponse.class
            );

            ApiResponse body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                NotificationView.showSuccess("Store review submitted successfully.");
            } else if (body != null) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                NotificationView.showError("Unknown error adding store review.");
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

    public List<ReviewDTO> getStoreReviews(int storeId) {
        String url = String.format("http://localhost:8080/api/Review/getStoreReviews?storeId=%d", storeId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<ApiResponse<List<ReviewDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<List<ReviewDTO>>>() {
            }
            );

            ApiResponse<List<ReviewDTO>> body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                return body.getData() != null ? body.getData() : List.of();
            } else if (body != null) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                NotificationView.showError("Unexpected empty response");
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

        return List.of(); // fallback if error
    }

    public void rankProduct(int storeId, String token, int productId, int newRank) {
        String url = String.format(
                "http://localhost:8080/stock/rankProduct?storeId=%d&token=%s&productId=%d&newRank=%d",
                storeId,
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                productId,
                newRank
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ApiResponse.class
            );

            ApiResponse body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                NotificationView.showSuccess("Product ranked successfully!");
            } else if (body != null) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                NotificationView.showError("Unknown error ranking product.");
            }
        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());
        }

    }

    public void addToCart(String token, ItemStoreDTO item, int quantity) {
        String url = String.format(
                "http://localhost:8080/api/users/addToCart?token=%s&storeId=%d&productId=%d&quantity=%d",
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                item.getStoreId(),
                item.getId(),
                quantity
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ApiResponse.class
            );

            ApiResponse body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                NotificationView.showSuccess("Added to cart successfully.");
            } else if (body != null) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                NotificationView.showError("Unexpected empty response.");
            }

        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    public List<ReviewDTO> getProductReviews(int storeId, int productId) {
        String url = String.format(
                "http://localhost:8080/api/Review/getProductReviews?storeId=%d&productId=%d",
                storeId, productId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse<List<ReviewDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<List<ReviewDTO>>>() {
            }
            );

            ApiResponse<List<ReviewDTO>> body = response.getBody();

            if (body != null && body.getErrNumber() == -1) {
                return body.getData() != null ? body.getData() : List.of();
            } else if (body != null) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                NotificationView.showError("Unexpected empty response");
            }

        } catch (Exception e) {
            NotificationView.showError("UNEXPECTED ERROR: " + e.getMessage());
        }

        return List.of();
    }

}
