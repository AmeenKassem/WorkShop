package workshop.demo.PresentationLayer.Presenter;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

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

                //UI.getCurrent().getPage().setLocation("/");
                // Reset user session to guest so MainLayout will show guest buttons
                VaadinSession.getCurrent().setAttribute("auth-token", null);
                VaadinSession.getCurrent().setAttribute("user-type", "guest");
                UI.getCurrent().getPage().executeJs("window.location.href='/'");
            }
        } catch (Exception e) {
            ExceptionHandlers.handleException(e);
        }
    }
}
