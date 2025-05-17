package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import workshop.demo.PresentationLayer.Presenter.InitPresenter;

@Route
@CssImport("./Theme/main-layout.css")
@JsModule("./notification.js")
public class MainLayout extends AppLayout {
    private InitPresenter presenter;
    VerticalLayout buttonColumn;

    public MainLayout() {

        addClassName("main-layout");
        this.presenter = new InitPresenter(this);
        createHeader();
        addRightSideButtons();

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // First handle the presenter's onAttach
        presenter.handleOnAttach("ws://localhost:8080/",
                VaadinSession.getCurrent().getAttribute("auth-role"));

        NotificationView notificationView = (NotificationView) VaadinSession.getCurrent()
                .getAttribute("notification-view");
        if (notificationView != null) {
            try {
                NotificationView newNotificationView = new NotificationView();
                newNotificationView.setReceivedNotification(notificationView.getReceivedNotifications());
                newNotificationView.register(UI.getCurrent());
                newNotificationView.createWS(UI.getCurrent(), (String) VaadinSession.getCurrent()
                        .getAttribute("username"));
                VaadinSession.getCurrent().setAttribute("notification-view", newNotificationView);
            } catch (Exception e) {
                System.out.println("Error registering notification view: " + e.getMessage());
            }
        }

    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        // Close the WebSocket when component is detached
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().executeJs(
                    "if(typeof window.closeNotificationSocket === 'function') { window.closeNotificationSocket(); }");
        }

        // Call presenter's onDetach if needed
        // presenter.handleOnDetach();
    }

    public void showError(String msg) {
        UI.getCurrent().access(() -> {
            Notification.show("❌ " + msg, 4000, Notification.Position.MIDDLE);
        });
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
    private void addRightSideButtons() {
        //Determine user type
        String userType = (String) VaadinSession.getCurrent().getAttribute("user-type");
        if (userType == null) {
            userType = "guest";
        }

        //Create a right-aligned vertical layout for buttons
        if (buttonColumn != null) {
            remove(buttonColumn); // Remove old one from navbar
            buttonColumn.removeAll();

        } else {
            buttonColumn = new VerticalLayout();
            buttonColumn.addClassName("right-button-column");
            buttonColumn.setSpacing(true);
            buttonColumn.setPadding(false);
            buttonColumn.setAlignItems(Alignment.END);

        }

        //Shared: My Cart
        RouterLink myCart = new RouterLink("My Cart", MyCartView.class);
        myCart.addClassName("right-button");
        buttonColumn.add(myCart);

        if (userType.equals("guest")) {
            //Guest buttons: sign up and login -> might mix it 
            RouterLink login = new RouterLink("Login", LoginView.class);
            RouterLink signUp = new RouterLink("Sign Up", RegisterView.class);
            login.addClassName("right-button");
            signUp.addClassName("right-button");
            buttonColumn.add(login, signUp);
        } else if (userType.equals("user")) {
            //Logged-in user buttons: myStores and log out, notification, open my own store
            RouterLink myStore = new RouterLink("My Stores", MyStoresView.class);
            myStore.addClassName("right-button");
            buttonColumn.add(myStore);
            //here must add also a button for notification

            //open my own store
            RouterLink openStore = new RouterLink("Open My Store", OpenStoreView.class); // <-- replace with your actual view
            openStore.addClassName("right-button");
            buttonColumn.add(openStore);

            // logout: Logout as a clickable Div (since RouterLink is for navigation)
            Div logout = new Div();
            logout.setText("Logout");
            logout.addClassName("right-button");
            logout.getStyle().set("cursor", "pointer");
            logout.addClickListener(e -> presenter.handleLogout());

            buttonColumn.add(logout);

        }
        addToNavbar(buttonColumn);

    }

    private void createHeader() {
        H1 logo = new H1("🛒 Click Market");
        logo.addClassName("market-title");

        Paragraph subtitle = new Paragraph(
                "Welcome to our market. We bring the best stores and products to your fingertips.\n"
                + "Join us and be an owner of your own store in a few clicks."
        );
        subtitle.addClassName("market-subtitle");

        VerticalLayout titleLayout = new VerticalLayout(logo, subtitle);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.addClassName("header-title");

        HorizontalLayout header = new HorizontalLayout(titleLayout);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName("app-header");
        header.setHeight("120px");
        addToNavbar(header);
    }

    public void refreshButtons() {
        if (buttonColumn != null) {
            buttonColumn.removeAll();
            remove(buttonColumn); // optional: if you want to re-add cleanly
        }
        addRightSideButtons(); // re-checks session and repopulates
    }
}