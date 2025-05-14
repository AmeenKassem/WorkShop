package workshop.demo.PresentationLayer.View;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import workshop.demo.DTOs.StoreDTO;
import workshop.demo.PresentationLayer.Presenter.HomePagePresenter;
import workshop.demo.PresentationLayer.Presenter.initPresenter;

@Route(value = "", layout = MainLayout.class)
@CssImport("./Theme/homePageTheme.css")
public class HomePage extends VerticalLayout {

    private final HomePagePresenter homePagePresenter;

    public HomePage() {
        this.homePagePresenter = new HomePagePresenter(this);
        setSizeFull();
        setSpacing(false);
        setPadding(true);
        setAlignItems(Alignment.START);
        addClassName("home-view");

        RouterLink login = new RouterLink("Login", LoginView.class);
        RouterLink register = new RouterLink("Register", RegisterView.class);
        //change it when making the cart page
        RouterLink myCart = new RouterLink("My Cart", MyCartView.class);
        login.addClassName("home-button");
        register.addClassName("home-button");
        myCart.addClassName("home-button");

        HorizontalLayout buttonRow = new HorizontalLayout(login, register, myCart);
        buttonRow.setWidthFull(); // take full width
        buttonRow.setJustifyContentMode(JustifyContentMode.START); // force align left
        buttonRow.addClassName("button-row");

        // === Placeholder for featured stores ===
        Label title = new Label("🔥 Featured Stores");
        title.getStyle().set("font-size", "24px").set("font-weight", "bold");

        // Fetch stores
        List<StoreDTO> stores = this.homePagePresenter.fetchStores();

        // Container for store cards
        Div storeContainer = new Div();
        storeContainer.addClassName("store-container");

        for (StoreDTO store : stores) {
            storeContainer.add(this.homePagePresenter.createStoreCard(store));
        }

        add(buttonRow, title, storeContainer);
    }

}
