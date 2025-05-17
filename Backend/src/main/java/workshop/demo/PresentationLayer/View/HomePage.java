package workshop.demo.PresentationLayer.View;

import java.util.List;

import org.springframework.stereotype.Component;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

import elemental.json.Json;
import elemental.json.JsonObject;
import workshop.demo.DTOs.NotificationDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.NotificationDTO.NotificationType;
import workshop.demo.PresentationLayer.Presenter.HomePagePresenter;

@Route(value = "", layout = MainLayout.class)
@CssImport("./Theme/homePageTheme.css")
public class HomePage extends VerticalLayout {

    private final HomePagePresenter homePagePresenter;

    public HomePage() {
        
        this.homePagePresenter = new HomePagePresenter(this);
        setSizeFull();
        setSpacing(false);
        setPadding(true);
        setAlignItems(Alignment.STRETCH);
        addClassName("home-view");

        showButtonsAsGorU();
        Span title = new Span("🔥 Featured Stores");
        title.getStyle().set("font-size", "24px").set("font-weight", "bold");

        // Fetch stores
        List<StoreDTO> stores = this.homePagePresenter.fetchStores();

        // Container for store cards
        Div storeContainer = new Div();
        storeContainer.addClassName("store-container");

        for (StoreDTO store : stores) {
            storeContainer.add(this.homePagePresenter.createStoreCard(store));
        }

        add(title, storeContainer);
    }

    private void showButtonsAsGorU() {
        //Determine user type
        String userType = (String) VaadinSession.getCurrent().getAttribute("user-type");
        if (userType == null) {
            userType = "guest";
        }

        //Create a right-aligned vertical layout for buttons
        VerticalLayout buttonColumn = new VerticalLayout();
        buttonColumn.setSpacing(true);
        buttonColumn.setPadding(false);
        buttonColumn.setAlignItems(Alignment.END);
        buttonColumn.addClassName("right-button-column");

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
            //Logged-in user buttons: myStores and log out
            RouterLink myStore = new RouterLink("My Stores", MyStoresView.class);
            myStore.addClassName("right-button");
            buttonColumn.add(myStore);

            // logout: Logout as a clickable Div (since RouterLink is for navigation)
            Div logout = new Div();
            logout.setText("Logout");
            logout.addClassName("right-button");
            logout.getStyle().set("cursor", "pointer");
            logout.addClickListener(e -> homePagePresenter.handleLogout());

            buttonColumn.add(logout);

        }

        add(buttonColumn);

    }

    public void showSuccess(String msg) {
        Notification.show("✅ " + msg, 3000, Notification.Position.MIDDLE);
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg, 5000, Notification.Position.MIDDLE);
    }

}
