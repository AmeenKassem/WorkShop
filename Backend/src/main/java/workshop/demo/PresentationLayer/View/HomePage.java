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
        setSizeFull();
        setSpacing(false);
        setPadding(true);
        setAlignItems(Alignment.STRETCH);
        addClassName("home-view");
        add(resultsContainer);

        //add search bar 
        add(createSearchBar());

        //showButtonsAsGorU();
        Span title = new Span("🔥 Featured Stores");
        title.getStyle().set("font-size", "24px").set("font-weight", "bold");
        // Container for store cards
        Div storeContainer = new Div();
        storeContainer.addClassName("store-container");
        // Fetch stores
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
        String userType = (String) VaadinSession.getCurrent().getAttribute("user-type");
        boolean isUser = "user".equals(userType);
        layout.setSpacing(false);
        layout.setPadding(true);
        layout.getStyle().set("background-color", "#f5f6ff").set("border-radius", "12px").set("padding", "20px");

        TextField searchField = new TextField("Search");
        searchField.setPlaceholder("Enter keyword or product name");
        searchField.setWidth("280px");

        ComboBox<Category> categoryCombo = new ComboBox<>("Category");
        categoryCombo.setItems(Category.values());
        categoryCombo.setPlaceholder("Select Category");
        categoryCombo.setWidth("200px");

        ComboBox<String> typeSelector = new ComboBox<>("Product Type");
        typeSelector.setItems("Normal", "Bid", "Auction", "Random Draw");
        typeSelector.setPlaceholder("Select Type");
        typeSelector.setRequired(true);
        typeSelector.setWidth("180px");

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
            String name = searchBy.equals("Product Name") ? inputText : null;
            String keyword = searchBy.equals("Keyword") ? inputText : null;

            resultsContainer.removeAll();

            if (!isUser) {
                // guest search: Normal only
                List<ItemStoreDTO> items = homePagePresenter.searchNormal(token, name, keyword, category, minPrice, maxPrice, productRate);
                if (items == null || items.isEmpty()) {
                    resultsContainer.add(new Paragraph("❌ No normal products found."));
                } else {
                    items.forEach(item -> resultsContainer.add(createItemCard(item)));
                }
                return;
            }
            //only for user:
            switch (selectedType) {
                case "Bid" -> {
                    List<BidDTO> bids = homePagePresenter.searchBids(token, name, keyword, category, minPrice, maxPrice, productRate);
                    if (bids == null || bids.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No bid products found."));
                        return;
                    }
                    bids.forEach(bid -> resultsContainer.add(createBidCard(bid)));
                }
                case "Auction" -> {
                    List<AuctionDTO> auctions = homePagePresenter.searchAuctions(token, name, keyword, category, minPrice, maxPrice, productRate);
                    if (auctions == null || auctions.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No auction products found."));
                        return;
                    }
                    auctions.forEach(auction -> resultsContainer.add(createAuctionCard(auction)));
                }
                case "Random Draw" -> {
                    List<RandomDTO> randoms = homePagePresenter.searchRandoms(token, name, keyword, category, minPrice, maxPrice, productRate);
                    if (randoms == null || randoms.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No random draw products found."));
                        return;
                    }
                    randoms.forEach(random -> resultsContainer.add(createRandomCard(random)));
                }
                default -> {
                    List<ItemStoreDTO> items = homePagePresenter.searchNormal(token, name, keyword, category, minPrice, maxPrice, productRate);
                    if (items == null || items.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No normal products found."));
                        return;
                    }
                    items.forEach(item -> resultsContainer.add(createItemCard(item)));
                }
            }
        });

        searchBtn.getStyle()
                .set("background-color", "#2E2E2E")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "12px")
                .set("padding", "6px 16px");

        HorizontalLayout row1;
        if (isUser) {
            row1 = new HorizontalLayout(searchField, categoryCombo, typeSelector, searchBySelector);
        } else {
            row1 = new HorizontalLayout(searchField, categoryCombo, searchBySelector);
        }
        HorizontalLayout row2 = new HorizontalLayout(minPriceField, maxPriceField, productRateCombo, searchBtn);
        row1.setSpacing(true);
        row2.setSpacing(true);
        row2.setDefaultVerticalComponentAlignment(Alignment.END);

        layout.add(row1, row2);
        return layout;
    }

    private Double parseDouble(String value) {
        try {
            return value != null && !value.isBlank() ? Double.parseDouble(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Div createItemCard(ItemStoreDTO item) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "12px")
                .set("border", "1px solid #ddd")
                .set("border-radius", "10px")
                .set("margin-bottom", "10px");

        Span name = new Span("🛍 " + item.getProductName());
        Paragraph store = new Paragraph("Store: " + item.getStoreName());
        Paragraph price = new Paragraph("Price: $" + item.getPrice());

        Button addToCart = new Button("Add to My Cart", e -> showAddToCartDialog(item));
        addToCart.getStyle().set("background-color", "#007bff").set("color", "white");

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

        HorizontalLayout buttons = new HorizontalLayout(submit, cancel);
        layout.add(quantityField, buttons);
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

        Span name = new Span("📢 Product Name: " + bid.productName);
        Paragraph store = new Paragraph("Store: " + bid.storeName);

        Button makeBid = new Button("Make a Bid", e -> showBidDialog(bid, bid.storeId));
        makeBid.getStyle().set("background-color", "#28a745").set("color", "white");

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

        Span name = new Span("🏁 Product Name: " + auction.productName);
        Paragraph store = new Paragraph("Store: " + auction.storeName);
        Paragraph max = new Paragraph("Max Bid: $" + auction.maxBid);

        String formattedTime = formatEndTime(auction.endTimeMillis);
        Paragraph endsAt = new Paragraph("⏰ Ends at: " + formattedTime);


        Button makeAuction = new Button("Make Auction", e -> showAuctionBidDialog(auction));
        makeAuction.getStyle()
                .set("background-color", "#2E2E2E")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "8px")
                .set("padding", "6px 16px");

        card.add(name, store, max,endsAt, makeAuction);
        return card;
    }

    private Div createRandomCard(RandomDTO random) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "12px")
                .set("border", "1px solid #aaa")
                .set("border-radius", "10px")
                .set("margin-bottom", "10px");

        Span name = new Span("🎲 Product Name: " + random.productName);
        Paragraph store = new Paragraph("Store: " + random.storeName);
        Paragraph amountLeft = new Paragraph("Left Amount: " + random.amountLeft);
        Paragraph price = new Paragraph("Price: $" + random.productPrice);

        String formattedEndTime = formatEndTime(random.endTimeMillis);
        Paragraph endTime = new Paragraph("🕒 Ends at: " + formattedEndTime);


        Button participate = new Button("Join Random Draw", e -> showRandomParticipationDialog(random));
        participate.getStyle().set("background-color", "#9c27b0").set("color", "white");

        card.add(name, store, amountLeft, price,endTime, participate);
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

}
