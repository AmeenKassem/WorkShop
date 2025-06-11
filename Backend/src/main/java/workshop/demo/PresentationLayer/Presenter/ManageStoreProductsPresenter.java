package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.ManageStoreProductsView;
import workshop.demo.PresentationLayer.View.NotificationView;

public class ManageStoreProductsPresenter {

    private final ManageStoreProductsView view;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public ManageStoreProductsPresenter(ManageStoreProductsView view) {
        this.view = view;
    }

    public void loadProducts(int storeId, String token) {
        //loads the products in this store:
        try {
            System.out.println("Fetching products for storeId: " + storeId);
            String url = "http://localhost:8080/stock/getProductsInStore?storeId=" + storeId;
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
            ApiResponse body = response.getBody();

            if (body != null && body.getErrNumber() == -1) {
                ItemStoreDTO[] items = mapper.convertValue(body.getData(), ItemStoreDTO[].class);

                Map<ItemStoreDTO, ProductDTO> mapped = new LinkedHashMap<>();
                for (ItemStoreDTO item : items) {
                    //try {
                    ProductDTO product = fetchProductDetails(token, item.getProductId());
                    mapped.put(item, product);
                    // } catch (Exception ex) {
                    //     System.out.println("⚠️ Failed to fetch product info for itemId " + item.getProductId());
                    // }
                }

                view.showProducts(mapped);
            } else {
                view.showEmptyPage("📭 No products in this store yet.");
                //NotificationView.showError("Error" + ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            }

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    private ProductDTO fetchProductDetails(String token, int productId) {

        try {
            String url = "http://localhost:8080/stock/getProductInfo?token="
                    + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8)
                    + "&productId=" + productId;

            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
            ApiResponse body = response.getBody();

            if (body != null && body.getErrNumber() == -1) {
                return mapper.convertValue(body.getData(), ProductDTO.class);
            }
        } catch (Exception ex) {
            //System.out.println("⚠️ Could not fetch product info: " + ex.getMessage());
            ExceptionHandlers.handleException(ex);
        }

        return new ProductDTO(productId, "(unknown)", null, "(no description)");
    }

    public void addProductToStore(int storeId, String token, String name, String desc,
            Category category, String price,
            String quantity, Dialog dialog) {

        try {
            String keywords = generateKeywordsFrom(name, desc);
            int productId = getOrCreateProductId(token, name, desc, category, keywords);

            String addItemUrl = "http://localhost:8080/stock/addItem?storeId=" + storeId
                    + "&token=" + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8)
                    + "&productId=" + productId
                    + "&quantity=" + UriUtils.encodeQueryParam(quantity, StandardCharsets.UTF_8)
                    + "&price=" + UriUtils.encodeQueryParam(price, StandardCharsets.UTF_8)
                    + "&category=" + category;

            restTemplate.postForEntity(addItemUrl, null, ApiResponse.class);
            NotificationView.showSuccess("Product added to store successfully.");
            dialog.close();
            loadProducts(storeId, token);

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    private int getOrCreateProductId(String token, String name, String desc, Category category, String keywords) throws Exception {
        String getAllUrl = "http://localhost:8080/stock//getAllProducts?token="
                + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8);
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(getAllUrl, ApiResponse.class);
        ProductDTO[] products = mapper.convertValue(response.getBody().getData(), ProductDTO[].class);
        for (ProductDTO product : products) {
            if (product.getName().equalsIgnoreCase(name)) {
                return product.getProductId();
            }
        }
        String addUrl = "http://localhost:8080/stock/addProduct?token="
                + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8)
                + "&name=" + UriUtils.encodeQueryParam(name, StandardCharsets.UTF_8)
                + "&description=" + UriUtils.encodeQueryParam(desc, StandardCharsets.UTF_8)
                + "&category=" + category
                + "&keywords=" + UriUtils.encodeQueryParam(keywords, StandardCharsets.UTF_8);
        ResponseEntity<ApiResponse> addResponse = restTemplate.postForEntity(addUrl, null, ApiResponse.class);
        return mapper.convertValue(addResponse.getBody().getData(), Integer.class);
    }

    public void deleteProduct(int storeId, String token, int productId) {
        try {
            String url = "http://localhost:8080/stock/removeItem?storeId=" + storeId
                    + "&token=" + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8)
                    + "&productId=" + productId;

            restTemplate.delete(url);
            NotificationView.showSuccess("Product removed.");
            loadProducts(storeId, token);
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
            //NotificationView.showError("Failed to remove product: " + e.getMessage());
        }
    }

    public void updateProduct(int storeId, String token, int productId, String quantity, String price, String description) {
        try {
            if (!quantity.isEmpty()) {

                String quantityUrl = "http://localhost:8080/stock/updateQuantity?storeId=" + storeId
                        + "&token=" + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8)
                        + "&productId=" + productId
                        + "&newQuantity=" + UriUtils.encodeQueryParam(quantity, StandardCharsets.UTF_8);
                restTemplate.postForEntity(quantityUrl, null, ApiResponse.class);
            }

            if (!price.isEmpty()) {
                String priceUrl = "http://localhost:8080/stock/updatePrice?storeId=" + storeId
                        + "&token=" + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8)
                        + "&productId=" + productId
                        + "&newPrice=" + UriUtils.encodeQueryParam(price, StandardCharsets.UTF_8);
                restTemplate.postForEntity(priceUrl, null, ApiResponse.class);

            }

            NotificationView.showSuccess("Product updated.");
            loadProducts(storeId, token);
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void loadAllProducts(String token, ComboBox<ProductDTO> comboBox, int storeId) {
        //load all the products in the sytem except the products in the store:
        try {
            // Get all products in the system
            String allUrl = "http://localhost:8080/stock/getAllProducts?token="
                    + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8);
            ResponseEntity<ApiResponse> allResponse = restTemplate.getForEntity(allUrl, ApiResponse.class);
            ProductDTO[] allProducts = mapper.convertValue(allResponse.getBody().getData(), ProductDTO[].class);

            // Get products already in this store
            String inStoreUrl = "http://localhost:8080/stock/getProductsInStore?storeId=" + storeId;
            ResponseEntity<ApiResponse> storeResponse = restTemplate.getForEntity(inStoreUrl, ApiResponse.class);
            ItemStoreDTO[] storeItems = mapper.convertValue(storeResponse.getBody().getData(), ItemStoreDTO[].class);

            Set<Integer> storeProductIds = Arrays.stream(storeItems)
                    .map(item -> item.getProductId())
                    .collect(Collectors.toSet());

            // Filter only products NOT in store
            List<ProductDTO> notInStore = Arrays.stream(allProducts)
                    .filter(p -> !storeProductIds.contains(p.getProductId()))
                    .toList();

            comboBox.setItems(notInStore);
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void addExistingProductAsItem(int storeId, String token, ProductDTO product, String price, String quantity, Dialog dialog) {
        try {
            String addItemUrl = "http://localhost:8080/stock/addItem"
                    + "?storeId=" + storeId
                    + "&token=" + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8)
                    + "&productId=" + product.getProductId()
                    + "&quantity=" + UriUtils.encodeQueryParam(quantity, StandardCharsets.UTF_8)
                    + "&price=" + UriUtils.encodeQueryParam(price, StandardCharsets.UTF_8)
                    + "&category=" + product.getCategory().name();

            restTemplate.postForEntity(addItemUrl, null, ApiResponse.class);
            NotificationView.showSuccess("Item added to your store.");
            dialog.close();
            loadProducts(storeId, token);
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void setProductToAuction(int storeId, String token, int productId, int quantity, long time, double startPrice) {
        String url = String.format(
                "http://localhost:8080/stock/setProductToAuction?token=%s&storeId=%d&productId=%d&quantity=%d&time=%d&startPrice=%.2f",
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                storeId, productId, quantity, time, startPrice
        );

        try {
            restTemplate.postForEntity(url, null, ApiResponse.class);
            NotificationView.showSuccess("Product set to auction!");
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void setProductToBid(int storeId, String token, int productId, int quantity) {
        try {
            String url = String.format(
                    "http://localhost:8080/stock/setProductToBid?token=%s&storeId=%d&productId=%d&quantity=%d",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    storeId, productId, quantity
            );

            restTemplate.postForEntity(url, null, ApiResponse.class);
            NotificationView.showSuccess("Product set to bid successfully!");
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void setProductToRandom(int storeId, String token, int productId, int quantity, double productPrice, long randomTime) {
        try {
            String url = String.format(
                    "http://localhost:8080/stock/setProductToRandom?token=%s&productId=%d&quantity=%d&productPrice=%.2f&storeId=%d&randomTime=%d",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    productId, quantity, productPrice, storeId, randomTime
            );

            restTemplate.postForEntity(url, null, ApiResponse.class);
            NotificationView.showSuccess("Product set to random draw!");
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    private String generateKeywordsFrom(String name, String desc) {
        return java.util.Arrays.stream((name + " " + desc).toLowerCase().split(" "))
                .map(w -> w.replaceAll("[^a-z0-9]", ""))
                .filter(w -> w.length() > 2)
                .distinct()
                .collect(java.util.stream.Collectors.joining(","));
    }

}
