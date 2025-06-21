package workshop.demo.PresentationLayer.View;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import workshop.demo.DTOs.StoreDTO;
import workshop.demo.PresentationLayer.Presenter.MyStoresPresenter;

@Route(value = "my stores", layout = MainLayout.class)
@CssImport("./Theme/myStoresTheme.css")
public class MyStoresView extends VerticalLayout {

    private final MyStoresPresenter presenter;

    public MyStoresView() {
        this.presenter = new MyStoresPresenter(this);
        addClassName("home-view");
        addOpenStoreButton();
        presenter.loadMyStores();
    }

    public void displayStores(List<StoreDTO> stores) {
        removeAll();
        addOpenStoreButton();

        if (stores == null || stores.isEmpty()) {
            Span empty = new Span("📭 You do not have any store YET!");
            empty.getStyle().set("font-size", "1.1rem").set("color", "#555");
            add(empty);
            return;
        }

        H2 title = new H2("Your Stores:");
        title.getStyle().set("color", "#1f2937").set("margin", "1rem 0");
        add(title);

        FlexLayout storeContainer = new FlexLayout();
        storeContainer.addClassName("store-container");
        storeContainer.setJustifyContentMode(JustifyContentMode.START);
        storeContainer.setAlignItems(Alignment.START);

        for (StoreDTO store : stores) {
            VerticalLayout card = new VerticalLayout();
            card.addClassName("store-card");

            Span name = new Span("🛍️ " + store.getStoreName());
            name.getStyle().set("font-weight", "bold").set("font-size", "1.2rem");

            Span category = new Span("🗂 Category: " + store.getCategory());
            category.getStyle().set("color", "#555");

            int filledStars = store.getFinalRating();
            int emptyStars = 5 - filledStars;
            String stars = "⭐".repeat(filledStars) + "☆".repeat(emptyStars);
            Span rating = new Span("Rating: " + stars);
            rating.addClassName("store-stars");

            Span status = new Span(store.isActive() ? "✅ Active" : "❌ Inactive");
            status.addClassName("store-status");
            if (!store.isActive()) {
                status.addClassName("inactive");
            }

            Button manageBtn = new Button("Manage My Store", e ->
                    UI.getCurrent().navigate("manageStore/" + store.getStoreId()));
            manageBtn.addClassName("store-button");

            card.add(name, category, rating, status, manageBtn);
            storeContainer.add(card);
        }
        add(storeContainer);
    }

    private void addOpenStoreButton() {
        RouterLink openStoreLink = new RouterLink();
        Button openStoreBtn = new Button("Open New Store");
        openStoreLink.setRoute(OpenStoreView.class);
        openStoreLink.add(openStoreBtn);

        openStoreBtn.addClassName("right-button");
        add(openStoreLink);
    }
}
