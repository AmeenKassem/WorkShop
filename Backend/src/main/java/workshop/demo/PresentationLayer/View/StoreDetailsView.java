package workshop.demo.PresentationLayer.View;

import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.ReviewDTO;
import workshop.demo.PresentationLayer.Presenter.StoreDetailsPresenter;

@Route(value = "store", layout = MainLayout.class)
public class StoreDetailsView extends VerticalLayout implements HasUrlParameter<Integer> {

    private int myStoreId;
    private final FlexLayout productContainer = new FlexLayout();
    private final StoreDetailsPresenter presenter;
    private final Div resultsContainer = new Div();

    public StoreDetailsView() {
        this.presenter = new StoreDetailsPresenter();
        add(new H1("Store Details "));
        add(resultsContainer);
     
        add(createSearchBar());
        HorizontalLayout storeActions = new HorizontalLayout();
        storeActions.setSpacing(true);

        Button showReviewsBtn = new Button("📖 Show Store Reviews", e -> showStoreReviewsDialog());
        Button addReviewBtn = new Button("📝 Add Review to Store", e -> openStoreReviewDialog());

        showReviewsBtn.getStyle()
        .set("background-color", "#ff9900")
        .set("color", "white")
        .set("font-weight", "bold")
        .set("border-radius", "12px")
        .set("padding", "8px 18px");

    addReviewBtn.getStyle()
        .set("background-color", "#ff9900")
        .set("color", "white")
        .set("font-weight", "bold")
        .set("border-radius", "12px")
        .set("padding", "8px 18px");


        storeActions.add(showReviewsBtn, addReviewBtn);
        add(storeActions);
        productContainer.setJustifyContentMode(FlexLayout.JustifyContentMode.START);
        productContainer.setAlignItems(FlexLayout.Alignment.START);
        productContainer.getStyle().set("gap", "1rem").set("margin-top", "1rem");

        add(productContainer);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer storeId) {
        if (storeId == null) {
            add(new Span(" No store ID provided."));
            return;
        }
        System.out.println("setParameter called with storeId = " + storeId);

        this.myStoreId = storeId;
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            add(new Span("You must be logged in to manage your store."));
            return;
        }
        // Load and show products
        try {
            Map<ItemStoreDTO, ProductDTO> itemsWithProducts = presenter.getProductsInStore(myStoreId, token);
            productContainer.removeAll();

            for (Map.Entry<ItemStoreDTO, ProductDTO> entry : itemsWithProducts.entrySet()) {
                Div card = createProductCard(entry.getKey(), entry.getValue());
                productContainer.add(card);
            }
        } catch (Exception e) {
            NotificationView.showError("Failed to load products: " + e.getMessage());
        }

    }

    private Div createProductCard(ItemStoreDTO item, ProductDTO product) {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        int storeId = item.getStoreId();
        Div card = new Div();
        card.getStyle()
                .set("border", "1px solid #ddd")
                .set("border-radius", "8px")
                .set("padding", "1rem")
                .set("width", "240px")
                .set("background-color", "#f9f9f9");

        H4 title = new H4("📦 " + item.getProductName());
        VerticalLayout actions = new VerticalLayout();
        actions.setSpacing(true);
        actions.setPadding(false);
        actions.setWidthFull();
        String stars = "⭐".repeat(item.getRank()) + "☆".repeat(5 - item.getRank());

        Paragraph rating = new Paragraph("⭐ Rank: " + stars);
        Paragraph price = new Paragraph("💰 Price: " + item.getPrice());
        Paragraph quantity = new Paragraph("📦 Quantity: " + item.getQuantity());
        Paragraph category = new Paragraph("🏷️ Category: " + product.category);
        Paragraph description = new Paragraph("📄 Description: " + product.getDescription());
        Button addToCart = new Button("🛒 Add to Cart", e -> openAddToCartDialog(item));
        //here manage the special  items:
        String userType = (String) VaadinSession.getCurrent().getAttribute("user-type");
        if (userType.equals("user")) {

            List<RandomDTO> randomProductIds = presenter.getRandomProductIds(myStoreId, token);
            List<AuctionDTO> auctionProductIds = presenter.getAuctionProductIds(myStoreId, token);
            List<BidDTO> bidProductIds = presenter.getBidProduct(myStoreId, token);
            //auction:
            boolean isAuction = auctionProductIds.stream()
                    .anyMatch(a -> a.productId == item.getProductId());
            if (isAuction) {
                AuctionDTO matchingAuction = auctionProductIds.stream()
                        .filter(a -> a.productId == item.getProductId())
                        .findFirst()
                        .orElse(null);

                if (matchingAuction != null) {
                    Button auctionButton = new Button("🎯 Join Auction", e -> showAuctionBidDialog(matchingAuction));
                    auctionButton.setWidthFull();
                    actions.add(auctionButton);

                }
            }

            //random:
            boolean isRandom = randomProductIds.stream()
                    .anyMatch(r -> r.productId == item.getProductId());
            if (isRandom) {
                RandomDTO matchingRandom = randomProductIds.stream()
                        .filter(r -> r.productId == item.getProductId())
                        .findFirst()
                        .orElse(null);

                if (matchingRandom != null) {
                    Button randomButton = new Button("🎲 Buy Random Card", e -> showRandomParticipationDialog(matchingRandom));
                    randomButton.setWidthFull();
                    actions.add(randomButton);
                }
            }
            //bid:
            boolean isBid = bidProductIds.stream()
                    .anyMatch(b -> b.productId == item.getProductId());

            if (isBid) {
                BidDTO matchingBid = bidProductIds.stream()
                        .filter(b -> b.productId == item.getProductId())
                        .findFirst()
                        .orElse(null);

                if (matchingBid != null) {
                    Button bidButton = new Button("💰 Make a Bid", e -> showBidDialog(matchingBid, storeId));
                    bidButton.setWidthFull();
                    actions.add(bidButton);
                }
            }
        }

        //other:
        Button addReview = new Button("💬 Add Review", e -> openProductReviewDialog(item));
        Button addRank = new Button("⭐ Add Rank", e -> openProductRankDialog(item));
        Button showReview = new Button("📖 Show Reviews", e -> openProductReviewsDialog(item));
        // Make all buttons full width
        addToCart.setWidthFull();
        addReview.setWidthFull();
        addRank.setWidthFull();
        showReview.setWidthFull();
        actions.add(addToCart, showReview, addReview, addRank);
        card.add(title, rating, price, quantity, category, description, actions);
        return card;

    }

    public void openProductRankDialog(ItemStoreDTO item) {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⭐ Rank Product: " + item.getProductName());

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        com.vaadin.flow.component.select.Select<Integer> rankSelect = new com.vaadin.flow.component.select.Select<>();
        rankSelect.setLabel("Select a rank");
        rankSelect.setItems(1, 2, 3, 4, 5);
        rankSelect.setPlaceholder("Choose...");

        Button submit = new Button("Submit", e -> {
            Integer rank = rankSelect.getValue();
            if (rank == null) {
                Notification.show("Please select a rank.");
                return;
            }
            presenter.rankProduct(item.getStoreId(), token, item.getProductId(), rank);
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(submit, cancel);
        layout.add(rankSelect, buttons);
        dialog.add(layout);
        dialog.open();
    }

    public void showDialog(List<String> reviews) {
        Dialog dialog = new Dialog();

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidth("400px");

        if (reviews == null || reviews.isEmpty()) {
            content.add(new Paragraph("There is nothing here yet."));
        } else {
            for (String review : reviews) {
                content.add(new Paragraph("💬 " + review));
            }
        }

        Button closeBtn = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(closeBtn);
        dialog.add(content);
        dialog.open();
    }

    private void showStoreReviewsDialog() {
        try {
            List<ReviewDTO> reviews = presenter.getStoreReviews(myStoreId);
            List<String> filteredReviews = reviews.stream()
                    .map(r -> "👤 " + r.getName() + ": " + r.getReviewMsg())
                    .toList();
            showDialog(filteredReviews);
        } catch (Exception e) {
            showDialog(List.of("Failed to load reviews: " + e.getMessage()));
        }
    }

    private void openProductReviewDialog(ItemStoreDTO item) {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Review for " + item.getProductName());

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidth("400px");

        TextArea input = new TextArea("Your Review");
        input.setWidthFull();
        input.setPlaceholder("Type something meaningful...");
        content.add(input);

        Button submit = new Button("Submit", e -> {
            String review = input.getValue();
            if (review == null || review.isBlank()) {
                NotificationView.showError("⚠️ Please enter a review.");
                return;
            }
            try {
                presenter.addReviewToItem(token, item.getStoreId(), item.getProductId(), review);
                //NotificationView.showSuccess("Product review submitted");
            } catch (Exception ex) {
                NotificationView.showError("Failed to submit: " + ex.getMessage());
            }
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        HorizontalLayout footer = new HorizontalLayout(submit, cancel);

        dialog.add(content);
        dialog.getFooter().add(footer);
        dialog.open();
    }

    private void openStoreReviewDialog() {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("📝 Review This Store");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidth("400px");

        TextArea input = new TextArea("Your Review");
        input.setWidthFull();
        input.setPlaceholder("What did you think about this store?");
        content.add(input);

        Button submit = new Button("Submit", e -> {
            String review = input.getValue();
            if (review == null || review.isBlank()) {
                Notification.show("⚠️ Please enter a review.");
                return;
            }
            try {
                presenter.addReviewToStore(token, myStoreId, review);
            } catch (Exception ex) {
                NotificationView.showError("Failed to submit: " + ex.getMessage());
            }
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        HorizontalLayout footer = new HorizontalLayout(submit, cancel);

        dialog.add(content);
        dialog.getFooter().add(footer);
        dialog.open();
    }

    private void openAddToCartDialog(ItemStoreDTO item) {
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

                presenter.addToCart(token, item, quantity);
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

    private void openProductReviewsDialog(ItemStoreDTO item) {
        try {
            List<ReviewDTO> reviews = presenter.getProductReviews(item.getStoreId(), item.getProductId());

            List<String> formatted = reviews.stream()
                    .map(r -> "👤 " + r.getName() + ": " + r.getReviewMsg())
                    .toList();

            showDialog(formatted);
        } catch (Exception e) {
            showDialog(List.of("Failed to load product reviews: " + e.getMessage()));
        }
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
            presenter.placeBidOnAuction(token, auction.auctionId, auction.storeId, price);
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

            presenter.participateInRandomDraw(token, random.id, random.storeId, amount, payment);
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
            presenter.addRegularBid(token, bid.bidId, storeId, price);
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(new HorizontalLayout(confirm, cancel));

        dialog.open();
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
        boolean isUser = "user".equals(userType);

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
                List<ItemStoreDTO> items = presenter.searchNormal(token, name, keyword, category, minPrice, maxPrice, productRate, myStoreId);
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
                    List<BidDTO> bids = presenter.searchBids(token, name, keyword, category, minPrice, maxPrice, productRate, myStoreId);
                    if (bids == null || bids.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No bid products found."));
                        return;
                    }
                    bids.forEach(bid -> resultsContainer.add(createBidCard(bid)));
                }
                case "Auction" -> {
                    List<AuctionDTO> auctions = presenter.searchAuctions(token, name, keyword, category, minPrice, maxPrice, productRate, myStoreId);
                    if (auctions == null || auctions.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No auction products found."));
                        return;
                    }
                    auctions.forEach(auction -> resultsContainer.add(createAuctionCard(auction)));
                }
                case "Random Draw" -> {
                    List<RandomDTO> randoms = presenter.searchRandoms(token, name, keyword, category, minPrice, maxPrice, productRate, myStoreId);
                    if (randoms == null || randoms.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No random draw products found."));
                        return;
                    }
                    randoms.forEach(random -> resultsContainer.add(createRandomCard(random)));
                }
                default -> {
                    List<ItemStoreDTO> items = presenter.searchNormal(token, name, keyword, category, minPrice, maxPrice, productRate, myStoreId);
                    if (items == null || items.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No normal products found."));
                        return;
                    }
                    items.forEach(item -> resultsContainer.add(createItemCard(item)));
                }
            }
        });
        searchBtn.getStyle()
        .set("background-color", "#ff9900") 
        .set("color", "white")
        .set("font-weight", "bold")
        .set("border-radius", "12px")
        .set("padding", "6px 16px")
        .set("font-size", "1rem");


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
    private Double parseDouble(String value) {
        try {
            return value != null && !value.isBlank() ? Double.parseDouble(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
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

        Button makeAuction = new Button("Make Auction", e -> showAuctionBidDialog(auction));
        makeAuction.getStyle()
                .set("background-color", "#2E2E2E")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "8px")
                .set("padding", "6px 16px");

        card.add(name, store, max, makeAuction);
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

        Button participate = new Button("Join Random Draw", e -> showRandomParticipationDialog(random));
        participate.getStyle().set("background-color", "#9c27b0").set("color", "white");

        card.add(name, store, amountLeft, price, participate);
        return card;
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

        Button addToCart = new Button("Add to My Cart", e -> openAddToCartDialog(item));
        addToCart.getStyle().set("background-color", "#007bff").set("color", "white");

        card.add(name, price, store, addToCart);
        return card;
    }

}
