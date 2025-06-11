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
import org.springframework.web.util.UriUtils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.MainLayout;
import workshop.demo.PresentationLayer.View.NotificationView;
import workshop.demo.PresentationLayer.View.PurchaseView;

public class InitPresenter {

    private final RestTemplate restTemplate;
    private final MainLayout view;

    public InitPresenter(MainLayout view) {
        this.view = view;
        this.restTemplate = new RestTemplate();
        initGuestIfNeeded();

    }

    private void initGuestIfNeeded() {
        Object token = VaadinSession.getCurrent().getAttribute("auth-token");
        Object role = VaadinSession.getCurrent().getAttribute("user-type");
        // If no token exists, this is a first-time guest
        if (token == null) {
            connectAsGuest();
        }
    }

    private void connectAsGuest() {
        try {
            String url = Base.url+"/api/users/generateGuest";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // Optional for GET/params
            HttpEntity<Void> entity = new HttpEntity<>(headers); // no body
            ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<String>>() {
            });
            ApiResponse body = response.getBody();
            if (body != null && body.getErrorMsg() == null) {
                String guestToken = (String) body.getData();

                VaadinSession.getCurrent().setAttribute("auth-token", guestToken);
                VaadinSession.getCurrent().setAttribute("user-type", "guest");

                System.out.println("Guest token stored: " + guestToken);
            } else {
                // view.showError(body.getErrorMsg());
                System.err.println(" Failed to generate guest: "
                        + (body != null ? body.getErrorMsg() : "null body"));
            }
        } catch (Exception e) {
            System.err.println(" Exception during guest generation: " + e.getMessage());
        }
    }

    public void handleOnAttach(String endpoint, Object user) {
        // Log who is currently attached to the UI
        initGuestIfNeeded();
        // connectAsGuest();

    }

    public void handleLogout() {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        String type = (String) VaadinSession.getCurrent().getAttribute("user-type");
        System.out.println("in logout -> presenter");
        System.out.println("token; " + token);
        System.out.println("the user type is: " + type);

        if (token != null) {
            System.out.println("the user type is: " + type);
            System.out.println("the token is: " + token);
            try {
                ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                        Base.url+"/api/users/logout?token=" + token,
                        null,
                        ApiResponse.class);

                ApiResponse body = response.getBody();
                if (body != null && body.getErrNumber() != -1) {
                    NotificationView.showError(ExceptionHandlers.getErrorMessage(body.getErrNumber()));

                }
                // Clear session and redirect

                System.out.println("session invalidated");

            } catch (HttpClientErrorException e) {
                ExceptionHandlers.handleException(e);
            }
        }
        // Clear session and redirect
        VaadinSession.getCurrent().setAttribute("auth-token", null);
        VaadinSession.getCurrent().setAttribute("user-type", "guest");
        UI.getCurrent().getPage().executeJs("window.closeNotificationSocket();");
        VaadinSession.getCurrent().getSession().invalidate();
        UI.getCurrent().getPage().setLocation("/");
    }

    public void handleReceiptsDisplay() {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            System.out.println("No token found, cannot fetch receipts.");
            NotificationView.showError(ExceptionHandlers.getErrorMessage(1001));
            return;
        }

        try {
            String url = String.format(
                    Base.url+"/api/history/getreceipts?token=%s",
                    UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<List<ReceiptDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<List<ReceiptDTO>>>() {
            });

            ApiResponse<List<ReceiptDTO>> responseBody = response.getBody();

            if (responseBody != null && responseBody.getErrNumber() == -1) {
                List<ReceiptDTO> receiptList = responseBody.getData();
                ReceiptDTO[] receipts = receiptList.toArray(new ReceiptDTO[0]);

                System.out.println("ok2");
                PurchaseView.showReceiptDialog(receipts);
            } else {
                NotificationView.showError(ExceptionHandlers.getErrorMessage(responseBody.getErrNumber()));
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);

        }

    }

}
