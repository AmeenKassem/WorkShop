package workshop.demo.PresentationLayer.View;

import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
//import com.vaadin.flow.component.page.AppShellConfigurator;
//import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

// Add a custom tag so the component can be found in the DOM
@Tag("notification-handler")
@JsModule("./notification.js")
public class NotificationView extends com.vaadin.flow.component.Component {

    // private static NotificationView instance;

    @Autowired
    public NotificationView() {

        // Important: Add this component to the UI when it's created
        getElement().executeJs(
                "const elem = this; " +
                        "setTimeout(() => {" +
                        "  console.log('NotificationView initialized and ready');" +
                        "}, 100);");
    }

    public static NotificationView create() {
        return new NotificationView();
    }

    /**
     * This method is called from the JavaScript side when a notification is
     * received.
     */

    @ClientCallable
    public void receiveNotification(String msg) {
        System.out.println("Java: Received notification in receiveNotification method: " + msg);

        UI ui = getUI().orElse(UI.getCurrent());
        if (ui != null) {
            ui.access(() -> {
                // Notification content
                Span title = new Span("🔔 Notification");
                title.getStyle()
                        .set("font-weight", "bold")
                        .set("font-size", "1.2rem")
                        .set("margin-bottom", "0.3rem")
                        .set("display", "block");

                Span body = new Span(msg);
                body.getStyle().set("font-size", "1.1rem");

                VerticalLayout layout = new VerticalLayout(title, body);
                layout.setPadding(false);
                layout.setSpacing(false);
                layout.setMargin(false);
                layout.getStyle().set("width", "100%");

                // Create a standard notification
                Notification notification = new Notification(layout); // auto-closes after 5 seconds
                notification.setDuration(5000);
                notification.setPosition(Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);

                notification.open();
            });
        } else {
            System.err.println("Warning: UI is null, notification could not be displayed");
        }
    }

    /**
     * Alternative method that might be called from JavaScript as a fallback.
     */
    @ClientCallable
    public void showNotification(String msg) {
        System.out.println("Java: Received notification in showNotification method: " + msg);
        receiveNotification(msg);
    }

    /**
     * Method to attach this component to any UI
     */
    public void register(UI ui) {
        ui.add(this);
        System.out.println("✅ NotificationView added to UI");
    }

}