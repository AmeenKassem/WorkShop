package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import workshop.demo.PresentationLayer.Presenter.InitPresenter;

@Route
@CssImport("./Theme/main-layout.css")
@JsModule("./notification.js")
public class MainLayout extends AppLayout{
    private InitPresenter presenter;
    
    public MainLayout() {
        addClassName("main-layout");
        this.presenter = new InitPresenter(this);

    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        // First handle the presenter's onAttach
        presenter.handleOnAttach("ws://localhost:8080/",
                VaadinSession.getCurrent().getAttribute("auth-role"));

        // // Register the notification view to this UI
        // UI.getCurrent().addAttachListener(event -> {
        //     NotificationView.register(event.getUI());

        // });
        
    }
    
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        
        // Close the WebSocket when component is detached
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().executeJs("if(typeof window.closeNotificationSocket === 'function') { window.closeNotificationSocket(); }");
        }
        
        // Call presenter's onDetach if needed
        // presenter.handleOnDetach();
    }
    
    public void showError(String msg) {
        UI.getCurrent().access(() -> {
            Notification.show("❌ " + msg, 4000, Notification.Position.MIDDLE);
        });
    }
}