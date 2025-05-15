package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.PresentationLayer.Presenter.InitPresenter;

@Route
@CssImport("./Theme/main-layout.css")
@JsModule("./notification-socket.js")
public class MainLayout extends AppLayout {

    private InitPresenter presenter;

    public MainLayout() {
        addClassName("main-layout");
        this.presenter = new InitPresenter(this);

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        presenter.handleOnAttach("ws://localhost:8080/",
                VaadinSession.getCurrent().getAttribute("auth-role"));

        VaadinSession session = VaadinSession.getCurrent();
        String username = (String) session.getAttribute("username");

        if (username != null && session.getAttribute("ws-initialized") == null) {
        UI.getCurrent().getPage().executeJs("window.initNotificationSocket($0);", username);
        session.setAttribute("ws-initialized", true);
        System.out.println("WebSocket initialized for user: " + username);
        } else {
            System.out.println("WebSocket already initialized or username is null.");
        }
    }

    @ClientCallable
    public void receiveNotification(String msg) {
        Notification.show("🔔 " + msg, 5000, Notification.Position.TOP_CENTER);
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg, 4000, Notification.Position.MIDDLE);
    }
    // @Override
    // protected void onDetach(DetachEvent detachEvent) {
    //     presenter.handleOnDetach();
    // private void createHeader() { // this should be in the presenter
    //     H1 logo = new H1("🛒 MarketAppMarket App");
    //     RouterLink login = new RouterLink("Login", LoginView.class);
    //     RouterLink register = new RouterLink("Register", RegisterView.class);
    //     HorizontalLayout header = new HorizontalLayout(logo, login, register);
    //     header.addClassName("app-header");
    //     addToNavbar(header);
    // }
}
