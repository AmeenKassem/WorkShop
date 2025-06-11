package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;
import org.springframework.http.*;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.PurchaseHistoryDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.AdminPurchaseHistoryView;
import workshop.demo.PresentationLayer.View.NotificationView;

public class AdminPurchaseHistoryPresenter {

    private final RestTemplate restTemplate;
    private final AdminPurchaseHistoryView view;

    public AdminPurchaseHistoryPresenter(AdminPurchaseHistoryView view) {
        this.view = view;
        this.restTemplate = new RestTemplate();

    }

    public void fetchPurchaseHistory() {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            NotificationView.showError(ExceptionHandlers.getErrorMessage(1001));
            return;
        }

        try {
            String url = String.format(
                    Base.url+"/api/users/purchaseHistory?token=%s",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<List<PurchaseHistoryDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
            });

            List<PurchaseHistoryDTO> historyList = response.getBody();
            this.view.displayPurchaseHistory(historyList);

        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }
}
