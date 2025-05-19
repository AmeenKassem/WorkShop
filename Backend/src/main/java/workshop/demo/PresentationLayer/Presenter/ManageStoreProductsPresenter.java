package workshop.demo.PresentationLayer.Presenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;
import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.ManageStoreProductsView;

import java.nio.charset.StandardCharsets;

public class ManageStoreProductsPresenter {

    private final ManageStoreProductsView view;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ManageStoreProductsPresenter(ManageStoreProductsView view) {
        this.view = view;
    }

    public void loadProducts(int storeId, String token) {
        String url = String.format("http://localhost:8080/api/stock/getStoreItems?storeId=%d&token=%s",
                storeId, UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8));
        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, ApiResponse.class);
            ApiResponse body = response.getBody();
            if (body != null && body.getErrorMsg() == null && body.getErrNumber() == -1) {
                view.showProducts(objectMapper.convertValue(body.getData(), objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, workshop.demo.DTOs.ItemStoreDTO.class)));
            } else {
                view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            }
        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = objectMapper.readValue(responseBody, ApiResponse.class);
                if (errorBody.getErrNumber() != -1) {
                    view.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    view.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                view.showError("HTTP error: " + e.getMessage());
            }
        } catch (Exception e) {
            view.showError("Unexpected error: " + e.getMessage());
        }
    }

    public void handleAddProductFlow(int storeId, String token, String productId, String quantity, String price, String category, String description, com.vaadin.flow.component.dialog.Dialog dialog) {
        String checkUrl = String.format("http://localhost:8080/api/products/exists?productId=%s", UriUtils.encodeQueryParam(productId, StandardCharsets.UTF_8));
        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(checkUrl, HttpMethod.GET, null, ApiResponse.class);
            ApiResponse body = response.getBody();
            if (body != null && Boolean.TRUE.equals(body.getData())) {
                addItemToStore(storeId, token, productId, quantity, price, category, description);
                dialog.close();
            } else {
                dialog.close();
                view.reopenAddProductDialogWithValues(productId, quantity, price, category, description);
            }
        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = objectMapper.readValue(responseBody, ApiResponse.class);
                if (errorBody.getErrNumber() != -1) {
                    view.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    view.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                view.showError("HTTP error: " + e.getMessage());
            }
        } catch (Exception e) {
            view.showError("Unexpected error: " + e.getMessage());
        }
    }

    private void addItemToStore(int storeId, String token, String productId, String quantity, String price, String category, String description) {
        try {
            String url = String.format("http://localhost:8080/api/stock/addItem?storeId=%d&token=%s&productId=%s&quantity=%s&price=%s&category=%s",
                    storeId,
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    UriUtils.encodeQueryParam(productId, StandardCharsets.UTF_8),
                    quantity,
                    price,
                    UriUtils.encodeQueryParam(category, StandardCharsets.UTF_8)
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ApiResponse.class);
            ApiResponse body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                view.showSuccess("Product added successfully to store");
            } else {
                view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            }
        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = objectMapper.readValue(responseBody, ApiResponse.class);
                if (errorBody.getErrNumber() != -1) {
                    view.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    view.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception parsingEx) {
                view.showError("HTTP error: " + e.getMessage());
            }
        } catch (Exception e) {
            view.showError("Unexpected error: " + e.getMessage());
        }
    }

    public void updateProduct(int storeId, String token, int productId, String quantity, String price, String description) {
        try {
            if (!quantity.isEmpty()) {
                String url = String.format("http://localhost:8080/api/stock/updateQuantity?storeId=%d&token=%s&productId=%d&newQuantity=%s",
                        storeId, UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8), productId, quantity);
                sendSimplePost(url);
            }
            if (!price.isEmpty()) {
                String url = String.format("http://localhost:8080/api/stock/updatePrice?storeId=%d&token=%s&productId=%d&newPrice=%s",
                        storeId, UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8), productId, price);
                sendSimplePost(url);
            }
        } catch (Exception e) {
            view.showError("Error updating product: " + e.getMessage());
        }
    }

    public void deleteProduct(int storeId, String token, int productId) {
        try {
            String url = String.format("http://localhost:8080/api/stock/removeItem?storeId=%d&token=%s&productId=%d",
                    storeId, UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8), productId);
            sendSimplePost(url);
        } catch (Exception e) {
            view.showError("Error deleting product: " + e.getMessage());
        }
    }

    public void addToSpecialPurchase(String token, int storeId, int productId, String type, String param1, String param2) {
        try {
            String endpoint = "";
            if (type.equalsIgnoreCase("Auction")) {
                endpoint = String.format("http://localhost:8080/api/stock/addToAuction?storeId=%d&token=%s&productId=%d&startPrice=%s",
                        storeId, UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8), productId, param1);
            } else if (type.equalsIgnoreCase("Lottery")) {
                endpoint = String.format("http://localhost:8080/api/stock/addToLottery?storeId=%d&token=%s&productId=%d&totalRequired=%s&participantLimit=%s",
                        storeId, UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8), productId, param1, param2);
            } else if (type.equalsIgnoreCase("Bid")) {
                endpoint = String.format("http://localhost:8080/api/stock/addToBid?storeId=%d&token=%s&productId=%d&minPrice=%s&deadline=%s",
                        storeId, UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8), productId, param1, param2);
            }
            sendSimplePost(endpoint);
        } catch (Exception e) {
            view.showError("Error adding to special purchase: " + e.getMessage());
        }
    }

    private void sendSimplePost(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<ApiResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ApiResponse.class);
            ApiResponse body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                view.showSuccess("Action completed successfully");
            } else {
                view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            }
        } catch (HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();
                ApiResponse errorBody = objectMapper.readValue(responseBody, ApiResponse.class);
                if (errorBody.getErrNumber() != -1) {
                    view.showError(ExceptionHandlers.getErrorMessage(errorBody.getErrNumber()));
                } else {
                    view.showError("FAILED: " + errorBody.getErrorMsg());
                }
            } catch (Exception ex) {
                view.showError("HTTP Error: " + e.getMessage());
            }
        } catch (Exception e) {
            view.showError("Unexpected error: " + e.getMessage());
        }
    }
}
