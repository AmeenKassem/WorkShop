package workshop.demo.PresentationLayer.View;

import java.util.List;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.PresentationLayer.Presenter.HomePagePresenter;

@Route(value = "", layout = MainLayout.class)
@CssImport("./Theme/homePageTheme.css")
@PageTitle("Home")
@Tag("div")
public class HomePage extends VerticalLayout {

    private final HomePagePresenter homePagePresenter;
    private final Div resultsContainer = new Div();

    public HomePage() {
        this.homePagePresenter = new HomePagePresenter(this);

        addClassName("home-view");
        setSizeFull();
        setSpacing(false);
        setPadding(true);
        setAlignItems(Alignment.STRETCH);

        add(createSearchBar()); // אזור חיפוש
        add(resultsContainer);  // תוצאות חיפוש (\u05deעל featured)

        // כותרת
        Span title = new Span("🔥 Featured Stores");
        title.addClassName("section-title");

        // רשת חנויות
        Div storeContainer = new Div();
        storeContainer.addClassName("store-container");


        List<StoreDTO> stores = this.homePagePresenter.fetchStores();
        if (stores == null || stores.isEmpty()) {
            Paragraph noStores = new Paragraph("No stores yet.");
            noStores.getStyle().set("font-size", "18px").set("color", "gray");
            storeContainer.add(noStores);
        } else {
            for (StoreDTO store : stores) {
                storeContainer.add(this.homePagePresenter.createStoreCard(store));
            }
        }

        add(title, storeContainer);
    }

    private VerticalLayout createSearchBar() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("search-panel");

        TextField searchField = new TextField("Search");
        searchField.setPlaceholder("Enter keyword or product name");
        searchField.setWidth("280px");

        ComboBox<Category> categoryCombo = new ComboBox<>("Category");
        categoryCombo.setItems(Category.values());
        categoryCombo.setPlaceholder("Select Category");
        categoryCombo.setWidth("200px");

        ComboBox<String> searchBySelector = new ComboBox<>("Search By");
        searchBySelector.setItems("Keyword", "Product Name");
        searchBySelector.setPlaceholder("Search By");
        searchBySelector.setRequired(true);
        searchBySelector.setWidth("180px");

        TextField minPriceField = new TextField("Min Price");
        minPriceField.setPlaceholder("Insert minimum price");
        minPriceField.setWidth("150px");

        TextField maxPriceField = new TextField("Max Price");
        maxPriceField.setPlaceholder("Insert maximum price");
        maxPriceField.setWidth("150px");

        ComboBox<Integer> productRateCombo = new ComboBox<>("Product Rate");
        productRateCombo.setItems(1, 2, 3, 4, 5);
        productRateCombo.setPlaceholder("Product rate (1-5)");
        productRateCombo.setWidth("160px");

        Button searchBtn = new Button("Search", event -> {
            String searchBy = searchBySelector.getValue();
            String inputText = searchField.getValue();
            Category category = categoryCombo.getValue();
            Double minPrice = parseDouble(minPriceField.getValue());
            Double maxPrice = parseDouble(maxPriceField.getValue());
            Integer productRate = productRateCombo.getValue();

            if (searchBy == null || inputText == null || inputText.isBlank()) {
                NotificationView.showError("Please fill all required fields.");
                return;
            }

            String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
            String name = searchBy.equals("Product Name") ? inputText : null;
            String keyword = searchBy.equals("Keyword") ? inputText : null;

            List<ItemStoreDTO> items = homePagePresenter.searchNormal(token, name, keyword, category, minPrice, maxPrice, productRate);
            resultsContainer.removeAll();
            if (items == null || items.isEmpty()) {
                NotificationView.showError("No results found.");
                return;
            }
            items.forEach(item -> resultsContainer.add(createItemCard(item)));
        });


        searchBtn.getStyle()
                .set("background-color", "#2E2E2E")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "12px")
                .set("padding", "6px 16px");

        HorizontalLayout row1 = new HorizontalLayout(searchField, categoryCombo, searchBySelector);
        HorizontalLayout row2 = new HorizontalLayout(minPriceField, maxPriceField, productRateCombo, searchBtn);
        row1.setSpacing(true);
        row2.setSpacing(true);
        row2.setDefaultVerticalComponentAlignment(Alignment.END);

        layout.add(row1, row2);
        return layout;
    }

    private Div createItemCard(ItemStoreDTO item) {
        Div card = new Div();
        card.addClassName("store-card");

        Span name = new Span("🍭 " + item.getProductName());
        Paragraph store = new Paragraph("Store: " + item.getStoreName());
        Paragraph price = new Paragraph("Price: ₪" + item.getPrice());

        Button addToCart = new Button("Add to My Cart", e -> showAddToCartDialog(item));
        addToCart.getStyle().set("background-color", "#007bff")
                            .set("color", "white")
                            .set("font-weight", "bold")
                            .set("border-radius", "8px")
                            .set("margin-top", "8px");

        card.add(name, price, store, addToCart);
        return card;
    }

    private void showAddToCartDialog(ItemStoreDTO item) {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("🛒 Add to Cart");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        TextField quantityField = new TextField("Quantity");
        quantityField.setPlaceholder("Enter quantity (e.g. 1)");
        quantityField.setWidthFull();

        Button submit = new Button("Add", event -> {
            try {
                int quantity = Integer.parseInt(quantityField.getValue());
                if (quantity <= 0) {
                    Notification.show("Quantity must be positive.");
                    return;
                }
                homePagePresenter.addToCart(token, item, quantity);
                dialog.close();
            } catch (NumberFormatException ex) {
                Notification.show("Please enter a valid number.");
            }
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        layout.add(quantityField, new HorizontalLayout(submit, cancel));
        dialog.add(layout);
        dialog.open();
    }

    private Double parseDouble(String value) {
        try {
            return value != null && !value.isBlank() ? Double.parseDouble(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
