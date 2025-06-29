package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.PresentationLayer.Presenter.InitPresenter;

//@Route
@CssImport("./Theme/main-layout.css")
@JsModule("./notification.js")
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private InitPresenter presenter;
    private VerticalLayout buttonColumn;

    public MainLayout() {
        addClassName("main-layout");
        this.presenter = new InitPresenter(this);
        createHeader();
        addRightSideButtons();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        //when finishing must change it to user-role
        Object roleAttr = VaadinSession.getCurrent().getAttribute("auth-role");
        String role = roleAttr != null ? roleAttr.toString() : "guest";

        presenter.handleOnAttach("ws://localhost:8080/", role);

        NotificationView existingView = (NotificationView) VaadinSession.getCurrent().getAttribute("notification-view");
        if (existingView != null) {
            try {
                NotificationView newView = new NotificationView();
                newView.setReceivedNotification(existingView.getReceivedNotifications());
                newView.register(UI.getCurrent());

                Object usernameAttr = VaadinSession.getCurrent().getAttribute("username");
                if (usernameAttr != null) {
                    newView.createWS(UI.getCurrent(), usernameAttr.toString());
                }

                VaadinSession.getCurrent().setAttribute("notification-view", newView);
            } catch (Exception e) {
                System.out.println("Error registering notification view: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().executeJs(
                    "if(typeof window.closeNotificationSocket === 'function') { window.closeNotificationSocket(); }");
        }
    }

    // @Override
    // public void beforeEnter(BeforeEnterEvent event) {
    //     System.out.println("in before enterrrrr main layout");
    //     if (!presenter.isSiteInitialized()) {
    //         String path = event.getLocation().getPath();
    //         if (!path.equals("admin/init") && !path.equals("no-init")) {
    //             event.forwardTo(NotInitializedView.class);
    //         }
    //     }
    // }
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        System.out.println("in before enterrrrr main layout");
        if (!presenter.isSiteInitialized()) {
            String path = event.getLocation().getPath();

            if (!path.equals("admin/init") && !path.equals("no-init")) {
                event.forwardTo(NotInitializedView.class);
            }
        }
    }

    private HorizontalLayout buttonRow;

    private void createHeader() {
        H1 logo = new H1("🛒 ShopPoint");
        logo.addClassName("market-title");
        logo.getStyle()
                .set("font-size", "2rem")
                .set("border-bottom", "2px solid #ce5290")
                .set("padding-bottom", "0.5rem")
                .set("margin-bottom", "1rem");

        // Vertical layout for the full header: logo on top, buttons below
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(Alignment.CENTER); 
        headerLayout.setSpacing(false);
        headerLayout.setPadding(true);
        headerLayout.addClassName("app-header");

        // Add the logo
        headerLayout.add(logo);

        // Prepare the horizontal layout for buttons
        buttonRow = new HorizontalLayout();
        buttonRow.setSpacing(false);
        buttonRow.setPadding(false);
        buttonRow.getStyle().set("gap", "0.5rem"); 
        buttonRow.setAlignItems(Alignment.CENTER);
        buttonRow.addClassName("header-button-row");
        headerLayout.add(buttonRow);

        // Add everything to the top nav
        addToNavbar(headerLayout);
    }

    private void addRightSideButtons() {
        if (buttonRow == null) {
            return;
        }
        buttonRow.removeAll();

        String userType = (String) VaadinSession.getCurrent().getAttribute("user-type");
        if (userType == null) {
            userType = "guest";
        }

        // Shared buttons
        RouterLink myCart = new RouterLink("My Cart", MyCartView.class);
        RouterLink homePage = new RouterLink("Home Page", HomePage.class);
        myCart.addClassName("right-button");
        homePage.addClassName("right-button");
        buttonRow.add(myCart, homePage);

        if (userType.equals("guest")) {
            RouterLink login = new RouterLink("Login", LoginView.class);
            login.addClassName("right-button");
            buttonRow.add(login);
        } else if (userType.equals("user") || userType.equals("admin")) {
            RouterLink myStore = new RouterLink("My Stores", MyStoresView.class);
            myStore.addClassName("right-button");

            Button notificationBtn = new Button("🧾 Notifications");
            notificationBtn.addClickListener(e -> {
                UI.getCurrent().getChildren()
                        .filter(c -> c instanceof NotificationView)
                        .map(NotificationView.class::cast)
                        .findFirst()
                        .ifPresent(NotificationView::openNotificationBill);
            });
            notificationBtn.addClassName("right-button");

            Button showReceipts = new Button("🧾 My Receipts");
            showReceipts.addClickListener(e -> presenter.handleReceiptsDisplay());
            showReceipts.addClassName("right-button");

            Div logout = new Div();
            logout.setText("Logout");
            logout.addClassName("right-button");
            logout.getStyle().set("cursor", "pointer");
            logout.addClickListener(e -> presenter.handleLogout());

            buttonRow.add(myStore, notificationBtn, showReceipts, logout);
        }

        if (userType.equals("admin")) {
            Button adminButton = new Button("Admin Panel", e -> UI.getCurrent().navigate("admin"));
            adminButton.addClassName("right-button");
            buttonRow.add(adminButton);
        }
    }

    public void refreshButtons() {
        if (buttonColumn != null) {
            buttonColumn.removeAll();
            remove(buttonColumn);
        }
        addRightSideButtons();
    }
}
