package workshop.demo.PresentationLayer.Presenter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DTOs.PurchaseHistoryDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.View.AdminPageView;
import workshop.demo.PresentationLayer.View.NotificationView;
import workshop.demo.PresentationLayer.View.PurchaseView;

public class AdminPagePresenter {

    private final RestTemplate restTemplate;
    private final AdminPageView view;

    public AdminPagePresenter(AdminPageView view) {
        this.restTemplate = new RestTemplate();
        this.view = view;

    }

    // public void onSuspendUser() {
    //     Notification.show("Suspending user -> TODO");
    // }
    // public void onPauseSuspension() {
    //     Notification.show("Pausing suspension for user -> TODO");
    // }
    // public void onResumeSuspension() {
    //     Notification.show("Resuming suspension for user -> TODO");
    // }
    // public void onRemoveUser() {
    //     Notification.show("Removing user -> TODO");
    // }
}
