package workshop.demo.PresentationLayer.Presenter;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.vaadin.flow.component.UI;

import workshop.demo.Controllers.ApiResponse;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.AdminInitView;
import workshop.demo.PresentationLayer.View.NotificationView;

public class AdminInitPresenter {

    private final AdminInitView view;
    private final RestTemplate restTemplate;

    public AdminInitPresenter(AdminInitView view) {
        this.restTemplate = new RestTemplate();
        this.view = view;
    }

    public void initializeSystem(String username, String password, String key) {
        try {
            // Build the URL
            String url = String.format(
                    Base.url + "/api/appsettings/admin/init?key=%s&userName=%s&password=%s",
                    key,
                    username,
                    password
            );

            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<ApiResponse>() {
            }
            );

            ApiResponse<?> res = response.getBody();

            if (res != null && res.getErrorMsg() == null) {
                NotificationView.showSuccess("Site has been successfully initialized.");
                // Optionally, redirect to the Home page
                UI.getCurrent().getPage().setLocation("/");
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }
}
