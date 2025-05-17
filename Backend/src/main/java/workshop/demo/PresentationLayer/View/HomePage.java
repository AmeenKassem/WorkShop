package workshop.demo.PresentationLayer.View;

import java.util.List;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import workshop.demo.DTOs.StoreDTO;
import workshop.demo.PresentationLayer.Presenter.HomePagePresenter;

@Route(value = "", layout = MainLayout.class)
@CssImport("./Theme/homePageTheme.css")
@PageTitle("Home")
@Tag("div")
public class HomePage extends VerticalLayout {

    private final HomePagePresenter homePagePresenter;

    public HomePage() {

        this.homePagePresenter = new HomePagePresenter(this);
        setSizeFull();
        setSpacing(false);
        setPadding(true);
        setAlignItems(Alignment.STRETCH);
        addClassName("home-view");

        //showButtonsAsGorU();
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

    public void showSuccess(String msg) {
        Notification.show("✅ " + msg, 3000, Notification.Position.MIDDLE);
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg, 5000, Notification.Position.MIDDLE);
    }

}
