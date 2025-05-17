package workshop.demo.PresentationLayer.View;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

//import com.nimbusds.jose.shaded.gson.JsonObject;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.page.AppShellConfigurator;
//import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.server.VaadinSession;

import elemental.json.Json;
import elemental.json.JsonObject;

// Add a custom tag so the component can be found in the DOM
@Tag("notification-handler")
@JsModule("./notification.js")
public class NotificationView extends com.vaadin.flow.component.Component {

    // private static NotificationView instance;
    private final List<JsonObject> receivedNotifications = new ArrayList<>();
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public NotificationView() {

        // Important: Add this component to the UI when it's created
        getElement().executeJs(
                "const elem = this; " +
                        "setTimeout(() => {" +
                        "  console.log('NotificationView initialized and ready');" +
                        "}, 100);");
    }

    public void setReceivedNotification(List<JsonObject> receivedNotifications) {
        this.receivedNotifications.clear();
        this.receivedNotifications.addAll(receivedNotifications);

    }

    public List<JsonObject> getReceivedNotifications() {
        return receivedNotifications;
    }

    private void storeNotification(JsonObject json) {
        receivedNotifications.add(json);
    }

    private void storeNotification(String rawMessage) {
        JsonObject json = Json.createObject();
        json.put("type", "text");
        json.put("message", rawMessage);
        receivedNotifications.add(json);
    }

    /**
     * This method is called from the JavaScript side when a notification is
     * received.
     */

    @ClientCallable
    public void receiveNotification(String msg) {
        System.out.println("Java: Received notification in receiveNotification method: " + msg);

        UI ui = getUI().orElse(UI.getCurrent());
        if (ui == null)
            return;

        ui.access(() -> {
            try {
                JsonObject json = Json.parse(msg);
                storeNotification(json);
                String type = json.getString("type");

                if ("OFFER".equals(type)) {
                    showOfferNotification(json);
                } else {
                    showSimpleNotification(msg); // fallback for plain string
                }

            } catch (Exception e) {
                System.out.println("hoong: " + e.getMessage());
                // Not JSON? Treat as regular string
                storeNotification(msg);
                showSimpleNotification(msg);
            }
        });
    }

    private void showSimpleNotification(String message) {
        Span title = new Span("🔔 Notification");
        title.getStyle().set("font-weight", "bold").set("font-size", "1.2rem");

        Span body = new Span(message);
        body.getStyle().set("font-size", "1.1rem");

        VerticalLayout layout = new VerticalLayout(title, body);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setMargin(false);

        Notification notification = new Notification(layout);
        notification.setDuration(4000);
        notification.setPosition(Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        notification.open();
    }

    private void showOfferNotification(JsonObject json) {
        String message = json.getString("message");

        Span body = new Span("💼 " + message);
        body.getStyle().set("font-size", "1.1rem");

        Notification notification = new Notification();
        notification.setDuration(0); // Manual close
        notification.setPosition(Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);

        Button approve = new Button("✅ Approve", e -> {
            // 🔽 This part is executed when Approve is clicked
            handleOfferDecision(true, json);
            Notification.show("You accepted the offer");
            notification.close();
        });

        Button decline = new Button("❌ Decline", e -> {
            // 🔽 This part is executed when Decline is clicked
            handleOfferDecision(false, json);
            Notification.show("You declined the offer");
            notification.close();
        });

        // ❌ Dismiss button
        Button closeButton = new Button("✖", e -> notification.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeButton.getStyle().set("margin-left", "auto");

        HorizontalLayout actions = new HorizontalLayout(approve, decline, closeButton);
        actions.setWidthFull();
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.getStyle().set("margin-top", "0.5rem");

        VerticalLayout layout = new VerticalLayout(body, actions);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setMargin(false);
        layout.getStyle().set("width", "100%");

        notification.add(layout);
        notification.open();
    }

    public void openNotificationBill() {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeight("400px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setSizeFull();

        for (JsonObject json : receivedNotifications) {
            String type = json.getString("type");

            if ("OFFER".equals(type)) {
                String msg = json.getString("message");

                Span offerMsg = new Span("📢 " + msg);

                // Container for the message + buttons (so we can remove it)
                VerticalLayout offerLayout = new VerticalLayout();
                offerLayout.setSpacing(false);
                offerLayout.setPadding(false);
                offerLayout.setMargin(false);

                Button approve = new Button("✅ Approve", e -> {
                    Notification.show("You accepted the offer");
                    content.remove(offerLayout); // remove this message
                });

                Button decline = new Button("❌ Decline", e -> {
                    Notification.show("You declined the offer");
                    content.remove(offerLayout); // remove this message
                });

                HorizontalLayout actionRow = new HorizontalLayout(approve, decline);
                offerLayout.add(offerMsg, actionRow);
                content.add(offerLayout);

            } else {
                String message = json.getString("message");
                Span textMsg = new Span("🔔 " + message);
                content.add(textMsg);
            }
        }

        Button closeBtn = new Button("Close", e -> dialog.close());
        content.add(closeBtn);

        dialog.add(content);
        dialog.open();
    }

    /**
     * Method to attach this component to any UI
     */
    public void createWS(UI ui, String username) {
        // Initialize WebSocket connection
        VaadinSession session = VaadinSession.getCurrent();

        if (username != null) {
            // Add a small delay to ensure JS is fully loaded
            ui = UI.getCurrent();
            if (ui != null) {
                try {
                    Page page = ui.getPage();

                    // First check if the notification.js is loaded
                    page.executeJs("console.log('Testing if JS module is loaded properly');");

                    // Then initialize the WebSocket with proper error handling
                    page.executeJs("try { " +
                            "console.log('About to initialize notification socket for: ' + $0); " +
                            "if (typeof window.initNotificationSocket === 'function') { " +
                            "  window.initNotificationSocket($0); " +
                            "  console.log('WebSocket initialization called'); " +
                            "} else { " +
                            "  console.error('initNotificationSocket function not found!'); " +
                            "} " +
                            "} catch(e) { console.error('Error initializing WebSocket:', e); }", username);

                    // Set the flag to avoid multiple initializations
                    session.setAttribute("ws-initialized", true);
                    // Register the notification view to this UI
                    // UI.getCurrent().addAttachListener(event -> {
                    // NotificationView.register(event.getUI());

                    // });
                    System.out.println("WebSocket initialization attempted for user: " + username);
                } catch (Exception e) {
                    System.out.println("Error creating WebSocket: " + e.getMessage());
                }
            }
        } else {
            System.out.println("Cannot initialize WebSocket: username is null");
        }

    }

    public void register(UI newUI) {
        // This method is not needed in the current context
        // But you can keep it for future use if needed
        newUI.add(this);
        System.out.println("✅ NotificationView added to UI");
    }

    public void handleOfferDecision(boolean decision, JsonObject json) {
        try {
            int storeId = Integer.parseInt(json.getString("storeId"));
            String senderName = json.getString("senderName");
            String receiverName = json.getString("receiverName");

            String url = String.format(
                    "http://localhost:8080/respondToOffer?storeId=%s&senderName=%s&receiverName=%s&answer=%b",
                    storeId,
                    URLEncoder.encode(senderName, StandardCharsets.UTF_8),
                    URLEncoder.encode(receiverName, StandardCharsets.UTF_8),
                    decision);

            // Send POST request
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Notification.show("Offer decision sent successfully");
            } else {
                Notification.show("Failed to send offer decision", 3000, Notification.Position.MIDDLE);
            }

        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            e.printStackTrace();
        }
    }

}