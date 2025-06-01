package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
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

    public Map<ItemStoreDTO, ProductDTO> getProductsInStore(int storeId, String token) throws Exception {
        String url = "http://localhost:8080/stock/getProductsInStore?storeId=" + storeId;
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
        ApiResponse<?> body = response.getBody();

        if (body == null || body.getErrorMsg() != null || body.getData() == null) {
            throw new Exception(body != null ? body.getErrorMsg() : "Failed to load products");
        }

        ItemStoreDTO[] items = mapper.convertValue(body.getData(), ItemStoreDTO[].class);
        //all product:
        String productsUrl = "http://localhost:8080/stock/getAllProducts?token="
                + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8);
        ResponseEntity<ApiResponse> productsResponse = restTemplate.getForEntity(productsUrl, ApiResponse.class);
        ApiResponse<?> productsBody = productsResponse.getBody();
        ProductDTO[] allProducts = mapper.convertValue(productsBody.getData(), ProductDTO[].class);

        // Map productId to ProductDTO
        Map<Integer, ProductDTO> productById = new HashMap<>();
        for (ProductDTO product : allProducts) {
            productById.put(product.getProductId(), product);
        }

        // Combine into final result
        Map<ItemStoreDTO, ProductDTO> result = new LinkedHashMap<>();
        for (ItemStoreDTO item : items) {
            ProductDTO matching = productById.get(item.getProductId());
            if (matching != null) {
                result.put(item, matching);
            }
        }
        return result;
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

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
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

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
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

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
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
            ExceptionHandlers.handleException(e);
        }

    }

    public void addToCart(String token, ItemStoreDTO item, int quantity) {
        Map<String, Object> body = new HashMap<>();
        body.put("item", item);
        body.put("quantity", quantity);
        String url = String.format(
                "http://localhost:8080/api/users/addToCart?token=%s",
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
            });
            ApiResponse<List<ReviewDTO>> body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                return body.getData() != null ? body.getData() : List.of();
            } else if (body != null) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                NotificationView.showError("Unexpected empty response");
            }

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
        return List.of();
    }

}
