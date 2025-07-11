package workshop.demo.PresentationLayer.View;

import java.time.LocalDateTime;
import java.util.List;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.RandomDTO;
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
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");

        LocalDateTime suspensionEnd = this.homePagePresenter.fetchSuspensionEndTime(token);
        if (suspensionEnd != null) {
            String formatted = suspensionEnd.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm"));
            Paragraph suspensionMsg = new Paragraph("🚫 You are suspended until: " + formatted);
            suspensionMsg.getStyle().set("color", "red").set("font-weight", "bold").set("font-size", "18px");
            add(suspensionMsg);
        }

        addClassName("home-view");
        setSizeFull();
        setSpacing(false);
        setPadding(true);
        setAlignItems(Alignment.CENTER);

        // Search bar
        VerticalLayout searchBarContainer = createSearchBar();
        searchBarContainer.addClassName("search-bar-container");
        add(searchBarContainer);

        resultsContainer.setWidthFull();
        resultsContainer.addClassName("results-grid");
        resultsContainer.getStyle().set("max-width", "1100px");
        add(resultsContainer);

        // Featured Stores
        VerticalLayout storeSection = new VerticalLayout();
        storeSection.addClassName("store-section");
        storeSection.setWidthFull();
        storeSection.setAlignItems(Alignment.START);

        Span title = new Span("🔥 Featured Stores");
        title.addClassName("section-title");
        storeSection.add(title);

        Div storeContainer = new Div();
        storeContainer.addClassName("store-container");

        List<StoreDTO> stores = homePagePresenter.fetchStores();
        if (stores == null || stores.isEmpty()) {
            Paragraph noStores = new Paragraph("No stores yet.");
            noStores.getStyle().set("font-size", "18px").set("color", "gray");
            storeContainer.add(noStores);
        } else {
            stores.forEach(store -> storeContainer.add(homePagePresenter.createStoreCard(store)));
        }

        storeSection.add(storeContainer);
        add(storeSection);
    }

    //------------------------------- for search:
    private VerticalLayout createSearchBar() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(true);
        layout.getStyle()
                .set("background-color", "#f5f6ff")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05)")
                .set("margin-bottom", "1.5rem");

        String userType = (String) VaadinSession.getCurrent().getAttribute("user-type");
        boolean isUser = "user".equals(userType) || "admin".equals(userType);

        TextField searchField = new TextField("Search");
        searchField.setPlaceholder("Enter keyword or product name");
        searchField.setWidth("200px");

        ComboBox<Category> categoryCombo = new ComboBox<>("Category");
        categoryCombo.setItems(Category.values());
        categoryCombo.setPlaceholder("Select Category");
        categoryCombo.setWidth("160px");

        ComboBox<String> typeSelector = new ComboBox<>("Product Type");
        typeSelector.setItems("Normal", "Bid", "Auction", "Random Draw");
        typeSelector.setPlaceholder("Select Type");
        typeSelector.setRequired(true);
        typeSelector.setWidth("150px");

        ComboBox<String> searchBySelector = new ComboBox<>("Search By");
        searchBySelector.setItems("Keyword", "Product Name");
        searchBySelector.setPlaceholder("Search By");
        searchBySelector.setRequired(true);
        searchBySelector.setWidth("150px");

        TextField minPriceField = new TextField("Min Price");
        minPriceField.setPlaceholder("Min");
        minPriceField.setWidth("100px");

        TextField maxPriceField = new TextField("Max Price");
        maxPriceField.setPlaceholder("Max");
        maxPriceField.setWidth("100px");

        ComboBox<Integer> productRateCombo = new ComboBox<>("Product Rate");
        productRateCombo.setItems(1, 2, 3, 4, 5);
        productRateCombo.setPlaceholder("1-5");
        productRateCombo.setWidth("120px");

        Button searchBtn = new Button("🔍 Search");
        searchBtn.addClassName("search-button");
        searchBtn.getStyle()
                .set("background", "linear-gradient(90deg, #ce5290, #f472b6)")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "12px")
                .set("padding", "8px 18px")
                .set("font-size", "1rem")
                .set("box-shadow", "0 2px 6px rgba(0, 0, 0, 0.1)");

        searchBtn.addClickListener(event -> {
            String selectedType = typeSelector.getValue();
            String searchBy = searchBySelector.getValue();
            String inputText = searchField.getValue();
            Category category = categoryCombo.getValue();
            Double minPrice = parseDouble(minPriceField.getValue());
            Double maxPrice = parseDouble(maxPriceField.getValue());
            Integer productRate = productRateCombo.getValue();

            if ((isUser && selectedType == null) || searchBy == null) {
                NotificationView.showError("Please select both product type and search mode.");
                return;
            }

            String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
            String name = "Product Name".equals(searchBy) ? inputText : null;
            String keyword = "Keyword".equals(searchBy) ? inputText : null;

            resultsContainer.removeAll();

            if (!isUser) {
                List<ItemStoreDTO> items = homePagePresenter.searchNormal(token, name, keyword, category, minPrice, maxPrice, productRate);
                if (items == null || items.isEmpty()) {
                    resultsContainer.add(new Paragraph("❌ No normal products found."));
                } else {
                    items.forEach(item -> resultsContainer.add(createItemCard(item)));
                }
                return;
            }

            switch (selectedType) {
                case "Bid" -> {
                    List<BidDTO> bids = homePagePresenter.searchBids(token, name, keyword, category, minPrice, maxPrice, productRate);
                    if (bids == null || bids.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No bid products found."));
                    } else {
                        bids.forEach(bid -> resultsContainer.add(createBidCard(bid)));
                    }
                }
                case "Auction" -> {
                    List<AuctionDTO> auctions = homePagePresenter.searchAuctions(token, name, keyword, category, minPrice, maxPrice, productRate);
                    if (auctions == null || auctions.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No auction products found."));
                    } else {
                        auctions.forEach(auction -> resultsContainer.add(createAuctionCard(auction)));
                    }
                }
                case "Random Draw" -> {
                    List<RandomDTO> randoms = homePagePresenter.searchRandoms(token, name, keyword, category, minPrice, maxPrice, productRate);
                    if (randoms == null || randoms.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No random draw products found."));
                    } else {
                        randoms.forEach(random -> resultsContainer.add(createRandomCard(random)));
                    }
                }
                default -> {
                    List<ItemStoreDTO> items = homePagePresenter.searchNormal(token, name, keyword, category, minPrice, maxPrice, productRate);
                    if (items == null || items.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No normal products found."));
                    } else {
                        items.forEach(item -> resultsContainer.add(createItemCard(item)));
                    }
                }
            }
        });

        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.getStyle()
                .set("flex-wrap", "wrap")
                .set("align-items", "end")
                .set("gap", "10px");

        if (isUser) {
            row.add(searchField, categoryCombo, typeSelector, searchBySelector, minPriceField, maxPriceField, productRateCombo, searchBtn);
        } else {
            row.add(searchField, categoryCombo, searchBySelector, minPriceField, maxPriceField, productRateCombo, searchBtn);
        }

        layout.add(row);
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
        card.addClassName("result-card");
        addToCart.addClassName("right-button");

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
                    NotificationView.showError("Quantity must be positive.");
                    return;
                }
                homePagePresenter.addToCart(token, item, quantity);
                dialog.close();
            } catch (NumberFormatException ex) {
                NotificationView.showError("Please enter a valid number.");
            }
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        layout.add(quantityField, new HorizontalLayout(submit, cancel));
        dialog.add(layout);
        dialog.open();
    }

    private Div createBidCard(BidDTO bid) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "12px")
                .set("border", "1px solid #ccc")
                .set("border-radius", "10px")
                .set("margin-bottom", "10px");

        Span name = new Span("📢 : " + bid.productName);
        Paragraph store = new Paragraph("🏬 Store : " + bid.storeName);

        Button makeBid = new Button("Make a Bid", e -> showBidDialog(bid, bid.storeId));
        makeBid.getStyle().set("background-color", "#28a745").set("color", "white");
        card.addClassName("result-card");
        makeBid.addClassName("right-button");

        card.add(name, store, makeBid);
        return card;
    }

    private Div createAuctionCard(AuctionDTO auction) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "12px")
                .set("border", "1px solid #bbb")
                .set("border-radius", "10px")
                .set("margin-bottom", "10px");

        Span name = new Span("🏁 " + auction.productName);
        Paragraph store = new Paragraph("🏬 Store : " + auction.storeName);
        Paragraph max = new Paragraph("💰 Max Bid: ₪" + auction.maxBid);

        String formattedTime = formatEndTime(auction.endTimeMillis);
        Paragraph endsAt = new Paragraph("⏰ Ends at: " + formattedTime);

        Button makeAuction = new Button("Make Auction", e -> showAuctionBidDialog(auction));
        card.addClassName("result-card");
        makeAuction.addClassName("right-button");

        card.add(name, store, max, endsAt, makeAuction);
        return card;
    }

    private Div createRandomCard(RandomDTO random) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "12px")
                .set("border", "1px solid #aaa")
                .set("border-radius", "10px")
                .set("margin-bottom", "10px");

        Span name = new Span("🎲: " + random.productName);
        Paragraph store = new Paragraph("🏬 Store: " + random.storeName);
        Paragraph amountLeft = new Paragraph("📦 Left: " + random.amountLeft);
        Paragraph price = new Paragraph("💰 Price: ₪" + random.productPrice);

        String formattedEndTime = formatEndTime(random.endTimeMillis);
        Paragraph endTime = new Paragraph("🕒 Ends at: " + formattedEndTime);

        Button participate = new Button("Join Random Draw", e -> showRandomParticipationDialog(random));
        participate.getStyle().set("background-color", "#9c27b0").set("color", "white");
        card.addClassName("result-card");
        participate.addClassName("right-button");

        card.add(name, store, amountLeft, price, endTime, participate);
        return card;
    }

    private String formatEndTime(long endMillis) {
        return java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a", java.util.Locale.ENGLISH)
                .withZone(java.time.ZoneId.systemDefault())
                .format(java.time.Instant.ofEpochMilli(endMillis));
    }

    private void showAuctionBidDialog(AuctionDTO auction) {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("💸 Place Your Bid in Auction");

        int suggestedPrice = (int) Math.ceil(auction.maxBid) + 1;

        IntegerField priceField = new IntegerField("Your Bid Price");
        priceField.setMin(suggestedPrice); // must be strictly higher
        priceField.setStepButtonsVisible(true);
        priceField.setValue(suggestedPrice); // suggest next valid price

        VerticalLayout layout = new VerticalLayout(
                new Paragraph("Current Highest Bid: " + auction.maxBid),
                priceField
        );
        dialog.add(layout);

        Button confirm = new Button("Place Bid in Auction", e -> {
            int price = priceField.getValue();
            homePagePresenter.placeBidOnAuction(token, auction.auctionId, auction.storeId, price);
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(new HorizontalLayout(confirm, cancel));

        dialog.open();
    }

    private void showRandomParticipationDialog(RandomDTO random) {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("🎲 Participate in Random Draw");

        // Input fields for payment
        TextField cardNumber = new TextField("Card Number");
        TextField cardHolder = new TextField("Cardholder Name");
        TextField expiration = new TextField("Expiration Date (MM/YY)");
        PasswordField cvv = new PasswordField("CVV");

        NumberField amountPaid = new NumberField("Amount Paid");
        amountPaid.setValue(random.productPrice);
        amountPaid.setMin(0.1);

        VerticalLayout form = new VerticalLayout(
                cardNumber, cardHolder, expiration, cvv, amountPaid
        );

        Button confirm = new Button("Confirm Purchase", e -> {
            PaymentDetails payment = new PaymentDetails(
                    cardNumber.getValue(),
                    cardHolder.getValue(),
                    expiration.getValue(),
                    cvv.getValue()
            );

            double amount = amountPaid.getValue();

            homePagePresenter.participateInRandomDraw(token, random.id, random.storeId, amount, payment);
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.add(form);
        dialog.getFooter().add(new HorizontalLayout(confirm, cancel));
        dialog.open();
    }

    private void showBidDialog(BidDTO bid, int storeId) {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("💰 Submit Your Bid Offer");

        IntegerField priceField = new IntegerField("Your Bid Price");
        priceField.setMin(1); // prevent 0 or negative bids
        priceField.setStepButtonsVisible(true);
        priceField.setValue(1); // default to 1

        VerticalLayout layout = new VerticalLayout(priceField);
        dialog.add(layout);

        Button confirm = new Button("Submit Bid", e -> {
            int price = priceField.getValue();
            homePagePresenter.addRegularBid(token, bid.bidId, storeId, price);
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(new HorizontalLayout(confirm, cancel));

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
