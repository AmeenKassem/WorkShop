package workshop.demo.PresentationLayer.Presenter;

import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.notification.Notification;

public class AdminPagePresenter {

    private final RestTemplate restTemplate;

    public AdminPagePresenter() {
        this.restTemplate = new RestTemplate();

    }

    public void onViewSystemPurchaseHistory() {
        Notification.show("Viewing system purchase history -> TODO");
    }

    public void onSuspendUser() {
        Notification.show("Suspending user -> TODO");
    }

    public void onPauseSuspension() {
        Notification.show("Pausing suspension for user -> TODO");
    }

    public void onResumeSuspension() {
        Notification.show("Resuming suspension for user -> TODO");
    }

    public void onRemoveUser() {
        Notification.show("Removing user -> TODO");
    }
}
