package workshop.demo.PresentationLayer.Presenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;
import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SingleBidDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.NotificationView;
import workshop.demo.PresentationLayer.View.ManageStoreSpecialPurchasesView;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ManageStoreSpecialPurchasesPresenter {
    private final ManageStoreSpecialPurchasesView view;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = Base.url + "/stock";
    private int storeId = -1;

    public ManageStoreSpecialPurchasesPresenter(ManageStoreSpecialPurchasesView view) {
        this.view = view;
    }

    private RandomDTO[] loadRandoms() {
        try {
            String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
            String url = String.format("%s/getAllRandomInStore?token=%s&storeId=%d", baseUrl,
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8), storeId);
            ResponseEntity<ApiResponse<RandomDTO[]>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<RandomDTO[]>>() {
                    });

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            } else {
                NotificationView.showError("Failed to load random draws.");
                return null;
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
        return null;
    }

    private AuctionDTO[] loadAuctions() {
        try {
            String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
            String url = String.format("%s/getAllAuctions?token=%s&storeId=%d", baseUrl,
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8), storeId);
            ResponseEntity<ApiResponse<AuctionDTO[]>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<AuctionDTO[]>>() {
                    });

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            } else {
                NotificationView.showError("Failed to load auctions.");
                return null;
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
        return null;
    }

    private BidDTO[] loadBids() {
        try {
            String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
            String url = String.format("%s/getAllBidsInStore?token=%s&storeId=%d", baseUrl,
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8), storeId);
            ResponseEntity<ApiResponse<BidDTO[]>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<BidDTO[]>>() {
                    });

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            } else {
                NotificationView.showError("Failed to load bids.");
                return null;
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
        return null;
    }

    public void respondToSingleBid(int bidId, int bidToRespondId, boolean accept, Double counterOffer) {
        try {
            String endpoint = accept ? "/acceptBid" : "/rejectBid";
            String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");

            StringBuilder urlBuilder = new StringBuilder(String.format("%s%s?token=%s&storeId=%d&bidId=%d",
                    baseUrl,
                    endpoint,
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8),
                    storeId,
                    bidId));

            if (accept) {
                urlBuilder.append("&userToAcceptForId=").append(bidToRespondId);
            } else {
                urlBuilder.append("&userToRejectForId=").append(bidToRespondId);
                if (counterOffer != null) {
                    urlBuilder.append("&ownerOffer=").append(counterOffer);
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                    urlBuilder.toString(),
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<String>>() {
                    });

            ApiResponse<String> body = response.getBody();
            if (body != null && body.getErrNumber() != -1) {
                NotificationView.showError("Failed to process bid response: " + body.getErrorMsg());
            } else {
                NotificationView.showSuccess("Bid has been " + (accept ? "accepted" : "rejected") + " successfully.");
                // view.refreshPage();
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }

    public void loadAllSpecials() {
        view.showAllSpecials(loadRandoms(), loadAuctions(), loadBids());
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
        loadAllSpecials();
    }
}
