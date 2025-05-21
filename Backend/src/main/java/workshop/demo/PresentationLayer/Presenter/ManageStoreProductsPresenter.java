package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.dialog.Dialog;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.ManageStoreProductsView;

public class ManageStoreProductsPresenter {

    private final ManageStoreProductsView view;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public ManageStoreProductsPresenter(ManageStoreProductsView view) {
        this.view = view;
    }

    public void loadProducts(int storeId, String token) {
    try {
        System.out.println("Fetching products for storeId: " + storeId);
        String url = "http://localhost:8080/stock/getProductsInStore?storeId=" + storeId;
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
        ApiResponse body = response.getBody();

        if (body != null && body.getErrNumber() == -1) {
            ItemStoreDTO[] items = mapper.convertValue(body.getData(), ItemStoreDTO[].class);

            Map<ItemStoreDTO, ProductDTO> mapped = new LinkedHashMap<>();
            for (ItemStoreDTO item : items) {
                try {
                    ProductDTO product = fetchProductDetails(token, item.getId());
                    mapped.put(item, product);
                } catch (Exception ex) {
                    System.out.println("⚠️ Failed to fetch product info for itemId " + item.getId());
                }
            }

            view.showProducts(mapped);
        } else {
            view.showEmptyPage("📭 No products in this store yet.");
            view.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
        }

    } catch (HttpClientErrorException e) {
        handleHttpClientError(e);
        view.showEmptyPage("❌ Failed to load products due to server error.");
    } catch (Exception e) {
        view.showEmptyPage("❌ Unexpected error occurred.");
        view.showError("Unexpected error: " + e.getMessage());
    }
}


    private ProductDTO fetchProductDetails(String token, int productId) {
    try {
        String url = "http://localhost:8080/stock/getProductInfo?token=" +
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8) +
                "&productId=" + productId;

        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
        ApiResponse body = response.getBody();

        if (body != null && body.getErrNumber() == -1) {
            return mapper.convertValue(body.getData(), ProductDTO.class);
        }
    } catch (Exception ex) {
        System.out.println("⚠️ Could not fetch product info: " + ex.getMessage());
    }

    return new ProductDTO(productId, "(unknown)", null, "(no description)");
}

    public void addProductToStore(int storeId, String token, String name, String desc,
                                  Category category, String keywords, String price,
                                  String quantity, Dialog dialog) {
        try {
            int productId = getOrCreateProductId(token, name, desc, category, keywords);

            String addItemUrl = "http://localhost:8080/stock/addItem?storeId=" + storeId +
                    "&token=" + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8) +
                    "&productId=" + productId +
                    "&quantity=" + UriUtils.encodeQueryParam(quantity, StandardCharsets.UTF_8) +
                    "&price=" + UriUtils.encodeQueryParam(price, StandardCharsets.UTF_8) +
                    "&category=" + category;

            restTemplate.postForEntity(addItemUrl, null, ApiResponse.class);
            view.showSuccess("Product added to store successfully.");
            dialog.close();
            loadProducts(storeId, token);

        } catch (HttpClientErrorException e) {
            handleHttpClientError(e);
        } catch (Exception e) {
            view.showError("Unexpected error while adding product: " + e.getMessage());
        }
    }

    private int getOrCreateProductId(String token, String name, String desc, Category category, String keywords) throws Exception {
        String getAllUrl = "http://localhost:8080/stock/getAllProducts?token=" +
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8);

        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(getAllUrl, ApiResponse.class);
        ProductDTO[] products = mapper.convertValue(response.getBody().getData(), ProductDTO[].class);

        for (ProductDTO product : products) {
            if (product.getName().equalsIgnoreCase(name)) {
                return product.getProductId();
            }
        }

        String addUrl = "http://localhost:8080/stock/addProduct?token=" +
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8) +
                "&name=" + UriUtils.encodeQueryParam(name, StandardCharsets.UTF_8) +
                "&description=" + UriUtils.encodeQueryParam(desc, StandardCharsets.UTF_8) +
                "&category=" + category +
                "&keywords=" + UriUtils.encodeQueryParam(keywords, StandardCharsets.UTF_8);

        ResponseEntity<ApiResponse> addResponse = restTemplate.postForEntity(addUrl, null, ApiResponse.class);
        return mapper.convertValue(addResponse.getBody().getData(), Integer.class);
    }

    public void deleteProduct(int storeId, String token, int productId) {
        try {
            String url = "http://localhost:8080/stock/removeItem?storeId=" + storeId +
                    "&token=" + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8) +
                    "&productId=" + productId;

            restTemplate.delete(url);
            view.showSuccess("Product removed.");
            loadProducts(storeId, token);
        } catch (Exception e) {
            view.showError("Failed to remove product: " + e.getMessage());
        }
    }

    public void updateProduct(int storeId, String token, int productId, String quantity, String price, String description) {
        try {
            if (!quantity.isEmpty()) {
                String quantityUrl = "http://localhost:8080/stock/updateQuantity?storeId=" + storeId +
                        "&token=" + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8) +
                        "&productId=" + productId +
                        "&newQuantity=" + UriUtils.encodeQueryParam(quantity, StandardCharsets.UTF_8);
                restTemplate.postForEntity(quantityUrl, null, ApiResponse.class);
            }

            if (!price.isEmpty()) {
                String priceUrl = "http://localhost:8080/stock/updatePrice?storeId=" + storeId +
                        "&token=" + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8) +
                        "&productId=" + productId +
                        "&newPrice=" + UriUtils.encodeQueryParam(price, StandardCharsets.UTF_8);
                restTemplate.postForEntity(priceUrl, null, ApiResponse.class);
            }

            view.showSuccess("Product updated.");
            loadProducts(storeId, token);
        } catch (HttpClientErrorException e) {
            handleHttpClientError(e);
        } catch (Exception e) {
            view.showError("Update failed: " + e.getMessage());
        }
    }

    private void handleHttpClientError(HttpClientErrorException e) {
        try {
            ApiResponse error = mapper.readValue(e.getResponseBodyAsString(), ApiResponse.class);
            if (error.getErrNumber() != -1) {
                view.showError(ExceptionHandlers.getErrorMessage(error.getErrNumber()));
            } else {
                view.showError("FAILED: " + error.getErrorMsg());
            }
        } catch (Exception parsingEx) {
            view.showError("HTTP error: " + e.getMessage());
        }
    }
}
