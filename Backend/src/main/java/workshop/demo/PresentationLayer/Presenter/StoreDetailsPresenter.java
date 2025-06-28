package workshop.demo.PresentationLayer.Presenter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.ReviewDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.NotificationView;

public class StoreDetailsPresenter {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String BASE_URL = Base.url + "/stock";

    public StoreDetailsPresenter() {
        this.restTemplate = new RestTemplate();
        this.mapper = new ObjectMapper();
    }

    public Map<ItemStoreDTO, ProductDTO> getProductsInStore(int storeId, String token) throws Exception {
        String url = Base.url + "/stock/getProductsInStore?storeId=" + storeId;
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);
        ApiResponse<?> body = response.getBody();

        if (body == null || body.getErrorMsg() != null || body.getData() == null) {
            throw new Exception(body != null ? body.getErrorMsg() : "Failed to load products");
        }

        ItemStoreDTO[] items = mapper.convertValue(body.getData(), ItemStoreDTO[].class);
        //all product:
        String productsUrl = Base.url + "/stock/getAllProducts?token="
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

    public void rankStore(String token, int storeId, int newRank) {
        String url = String.format(
                Base.url + "/api/store/rankStore?token=%s&storeId=%d&newRank=%d",
                UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                storeId,
                newRank
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
                //NotificationView.showSuccess("Store ranked successfully.");
            } else if (body != null) {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));
            } else {
                NotificationView.showError("Unknown error ranking the store.");
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void addReviewToItem(String token, int storeId, int productId, String review) {
        String url = String.format(
                Base.url + "/api/Review/addToProduct?token=%s&storeId=%d&productId=%d&review=%s",
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
                Base.url + "/api/Review/addToStore?token=%s&storeId=%d&review=%s",
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
        String url = String.format(Base.url + "/api/Review/getStoreReviews?storeId=%d", storeId);

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
                Base.url + "/stock/rankProduct?storeId=%d&token=%s&productId=%d&newRank=%d",
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
                Base.url + "/api/users/addToCart?token=%s",
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
                Base.url + "/api/Review/getProductReviews?storeId=%d&productId=%d",
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

    public List<RandomDTO> getRandomProductIds(int storeId, String token) {
        try {
            String url = String.format(
                    Base.url + "/stock/getAllRandomInStore?token=%s&storeId=%d",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    storeId
            );

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);

            if (response.getBody() != null && response.getBody().getErrNumber() == -1) {
                ObjectMapper mapper = new ObjectMapper();
                RandomDTO[] randoms = mapper.convertValue(response.getBody().getData(), RandomDTO[].class);
                return Arrays.asList(randoms);
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }

        return new ArrayList<>();
    }

    public List<AuctionDTO> getAuctionProductIds(int storeId, String token) {
        try {
            String url = String.format(
                    Base.url + "/stock/getAllAuctions?token=%s&storeId=%d",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    storeId
            );
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);

            if (response.getBody() != null && response.getBody().getErrNumber() == -1) {
                ObjectMapper mapper = new ObjectMapper();
                AuctionDTO[] auctions = mapper.convertValue(response.getBody().getData(), AuctionDTO[].class);
                return Arrays.asList(auctions);

            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
        return new ArrayList<>();
    }

    public void placeBidOnAuction(String token, int auctionId, int storeId, int price) {
        try {
            String url = String.format(
                    Base.url + "/stock/addBidOnAuction?token=%s&auctionId=%d&storeId=%d&price=%d",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    auctionId, storeId, price
            );

            restTemplate.postForEntity(url, null, ApiResponse.class);
            NotificationView.showSuccess(" Bid placed successfully!");
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }

    }

    public void participateInRandomDraw(String token, int randomId, int storeId, double amountPaid, PaymentDetails payment) {
        try {
            String url = String.format(
                    Base.url + "/purchase/participateRandom?token=%s&randomId=%d&storeId=%d&amountPaid=%.2f",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    randomId, storeId, amountPaid
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PaymentDetails> entity = new HttpEntity<>(payment, headers);

            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ApiResponse.class
            );

            NotificationView.showSuccess("Successfully participated in the random draw!");

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }

    }

    public List<BidDTO> getBidProduct(int storeId, String token) {
        try {
            String url = String.format(
                    Base.url + "/stock/getAllBidsInStore?token=%s&storeId=%d",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    storeId
            );

            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);

            if (response.getBody() != null && response.getBody().getErrNumber() == -1) {
                ObjectMapper mapper = new ObjectMapper();
                BidDTO[] bids = mapper.convertValue(response.getBody().getData(), BidDTO[].class);
                return Arrays.asList(bids);
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
        return new ArrayList<>();
    }

    public boolean addRegularBid(String token, int bidId, int storeId, double price) {
        try {
            String url = String.format(
                    Base.url + "/stock/addRegularBid?token=%s&bidId=%d&storeId=%d&offer=%s",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    bidId,
                    storeId,
                    Double.toString(price)
            );

            ResponseEntity<ApiResponse> response = restTemplate.postForEntity(url, null, ApiResponse.class);

            if (response.getBody() != null && response.getBody().getErrNumber() == -1) {
                NotificationView.showSuccess("Bid placed successfully!");
                return true;
            }

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }

        return false;
    }

    // public List<ItemStoreDTO> searchNormal(String token, String name, String keyword, Category category,
    //         Double minPrice, Double maxPrice, Integer productRate, Integer storeId) {
    //     try {
    //         String url = buildUrl("/searchProducts", token, name, keyword, category, minPrice, maxPrice, productRate, storeId);
    //         ResponseEntity<ApiResponse<ItemStoreDTO[]>> response = restTemplate.exchange(
    //                 url,
    //                 HttpMethod.GET,
    //                 new HttpEntity<>(buildHeaders()),
    //                 new ParameterizedTypeReference<>() {
    //         });
    //         ApiResponse<ItemStoreDTO[]> body = response.getBody();
    //         if (body != null && body.getErrNumber() == -1) {
    //             return List.of(body.getData());
    //         }
    //     } catch (Exception e) {
    //         ExceptionHandlers.handleException(e);
    //     }
    //     return Collections.emptyList();
    // }
    public List<ItemStoreDTO> searchNormal(String token, String name, String keyword, Category category,
            Double minPrice, Double maxPrice, Integer productRate, Integer storeId) {
        try {
            StringBuilder url = new StringBuilder(Base.url + "/stock/searchProducts?token=")
                    .append(URLEncoder.encode(token, StandardCharsets.UTF_8));

            if (name != null) {
                url.append("&productNameFilter=").append(URLEncoder.encode(name, StandardCharsets.UTF_8));
            }
            if (keyword != null) {
                url.append("&keywordFilter=").append(URLEncoder.encode(keyword, StandardCharsets.UTF_8));
            }
            if (category != null) {
                url.append("&categoryFilter=").append(category.name());
            }
            if (minPrice != null) {
                url.append("&minPrice=").append(minPrice);
            }
            if (maxPrice != null) {
                url.append("&maxPrice=").append(maxPrice);
            }
            if (productRate != null) {
                url.append("&minProductRating=").append(productRate);
            }
            if (storeId != null) {
                url.append("&storeId=").append(storeId);
            }

            System.out.println("➡️ Final search URL: " + url); // For debugging

            ResponseEntity<ApiResponse<ItemStoreDTO[]>> response = restTemplate.exchange(
                    url.toString(),
                    HttpMethod.GET,
                    new HttpEntity<>(buildHeaders()),
                    new ParameterizedTypeReference<>() {
            });

            ApiResponse<ItemStoreDTO[]> body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                return List.of(body.getData());
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
        return Collections.emptyList();
    }

    public List<BidDTO> searchBids(String token, String name, String keyword, Category category,
            Double minPrice, Double maxPrice, Integer productRate, Integer storeId) {
        try {
            String url = buildUrl("/searchBids", token, name, keyword, category, minPrice, maxPrice, productRate, storeId);
            ResponseEntity<ApiResponse<BidDTO[]>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(buildHeaders()),
                    new ParameterizedTypeReference<>() {
            }
            );
            ApiResponse<BidDTO[]> body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                return List.of(body.getData());
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
        return Collections.emptyList();
    }

    public List<AuctionDTO> searchAuctions(String token, String name, String keyword, Category category,
            Double minPrice, Double maxPrice, Integer productRate, Integer storeId) {
        try {
            String url = buildUrl("/searchAuctions", token, name, keyword, category, minPrice, maxPrice, productRate, storeId);
            ResponseEntity<ApiResponse<AuctionDTO[]>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(buildHeaders()),
                    new ParameterizedTypeReference<>() {
            }
            );
            ApiResponse<AuctionDTO[]> body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                return List.of(body.getData());
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
        return Collections.emptyList();
    }

    public List<RandomDTO> searchRandoms(String token, String name, String keyword, Category category,
            Double minPrice, Double maxPrice, Integer productRate, Integer storeId) {
        try {
            String url = buildUrl("/searchRandoms", token, name, keyword, category, minPrice, maxPrice, productRate, storeId);
            ResponseEntity<ApiResponse<RandomDTO[]>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(buildHeaders()),
                    new ParameterizedTypeReference<>() {
            }
            );
            ApiResponse<RandomDTO[]> body = response.getBody();
            if (body != null && body.getErrNumber() == -1) {
                return List.of(body.getData());
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
        return Collections.emptyList();
    }

    private String buildUrl(String path, String token, String name, String keyword, Category category,
            Double minPrice, Double maxPrice, Integer productRate, Integer storeId) {

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
        if (storeId != null) {
            builder.queryParam("storeId", storeId);
        }

        return builder.toUriString();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

}
