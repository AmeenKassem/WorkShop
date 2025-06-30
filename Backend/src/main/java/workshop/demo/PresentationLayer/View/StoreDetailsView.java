package workshop.demo.PresentationLayer.View;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
import workshop.demo.DTOs.AuctionStatus;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.ReviewDTO;
import workshop.demo.PresentationLayer.Presenter.StoreDetailsPresenter;

@Route(value = "store", layout = MainLayout.class)
@CssImport("./Theme/storeDetails.css")
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
        Button addStoreRankBtn = new Button("⭐ Add Store Rank", e -> openStoreRankDialog());
        addStoreRankBtn.addClassName("store-action-button");
        showReviewsBtn.addClassName("store-action-button");
        addReviewBtn.addClassName("store-action-button");

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

        addStoreRankBtn.getStyle()
                .set("background-color", "#10b981")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "12px")
                .set("padding", "8px 18px");

        storeActions.add(showReviewsBtn, addReviewBtn, addStoreRankBtn);
        add(storeActions);
        productContainer.setJustifyContentMode(FlexLayout.JustifyContentMode.START);
        productContainer.setAlignItems(FlexLayout.Alignment.START);
        productContainer.getStyle().set("gap", "1rem").set("margin-top", "1rem");
        productContainer.addClassName("product-container");

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
        card.addClassName("product-card");

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
        if (userType.equals("user") || userType.equals("admin")) {

            List<RandomDTO> randomProductIds = presenter.getRandomProductIds(myStoreId, token);
            List<AuctionDTO> auctionProductIds = presenter.getAuctionProductIds(myStoreId, token);
            List<BidDTO> bidProductIds = presenter.getBidProduct(myStoreId, token);
            //auction:
            List<AuctionDTO> matchingAuctions = auctionProductIds.stream()
                    .filter(a -> a.productId == item.getProductId())
                    .collect(Collectors.toList());
            if (!matchingAuctions.isEmpty()) {
                Button showAuctionsButton = new Button("🎯 Show Auctions", e -> openAllAuctionsDialog(matchingAuctions, item.getProductId()));
                showAuctionsButton.setWidthFull();
                actions.add(showAuctionsButton);
            }

            //random:
            List<RandomDTO> matchingRandoms = randomProductIds.stream()
                    .filter(r -> r.productId == item.getProductId())
                    .collect(Collectors.toList());

            if (!matchingRandoms.isEmpty()) {
                Button showRandomsButton = new Button("🎲 Show Random Cards", e -> openAllRandomsDialog(matchingRandoms, item.getProductId()));
                showRandomsButton.setWidthFull();
                actions.add(showRandomsButton);
            }
            //bid:
            List<BidDTO> matchingBids = bidProductIds.stream()
                    .filter(b -> b.productId == item.getProductId())
                    .collect(Collectors.toList());

            if (!matchingBids.isEmpty()) {
                Button showBidsButton = new Button("💰 Show Bids", e -> openAllBidsDialog(matchingBids, item.getProductId(), storeId));
                showBidsButton.setWidthFull();
                actions.add(showBidsButton);
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
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        layout.setWidth("300px");

        com.vaadin.flow.component.select.Select<Integer> rankSelect = new com.vaadin.flow.component.select.Select<>();
        rankSelect.setLabel("Select a rank");
        rankSelect.setItems(1, 2, 3, 4, 5);
        rankSelect.setPlaceholder("Choose...");
        rankSelect.setWidthFull();

        Button submit = new Button("Submit", e -> {
            Integer rank = rankSelect.getValue();
            if (rank == null) {
                NotificationView.showError("⚠️ Please select a rank.");
                return;
            }
            presenter.rankProduct(item.getStoreId(), token, item.getProductId(), rank);
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());

        submit.getStyle()
                .set("background", "linear-gradient(90deg, #10b981, #059669)") // ירוק יפה
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "8px")
                .set("padding", "6px 12px");

        cancel.getStyle()
                .set("background-color", "#e5e7eb")
                .set("color", "#374151")
                .set("border-radius", "8px")
                .set("padding", "6px 12px");

        HorizontalLayout buttons = new HorizontalLayout(submit, cancel);
        buttons.setSpacing(true);

        layout.add(rankSelect, buttons);
        dialog.add(layout);
        dialog.open();
    }

    public void showDialog(List<String> reviews) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("📖 Store Reviews");
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setWidth("400px");
        content.getStyle()
                .set("background-color", "#f9fafb")
                .set("border-radius", "8px");

        if (reviews == null || reviews.isEmpty()) {
            content.add(new Paragraph("⚠️ There is nothing here yet."));
        } else {
            for (String review : reviews) {
                Paragraph p = new Paragraph(review);
                p.getStyle()
                        .set("background-color", "#f3f4f6")
                        .set("padding", "8px")
                        .set("border-radius", "6px")
                        .set("font-size", "0.95rem")
                        .set("color", "#374151");
                content.add(p);
            }
        }

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.getStyle()
                .set("background", "linear-gradient(90deg, #6b7280, #4b5563)")
                .set("color", "white")
                .set("border-radius", "8px")
                .set("padding", "6px 12px")
                .set("font-weight", "bold");

        dialog.add(content);
        dialog.getFooter().add(closeBtn);
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
        dialog.setHeaderTitle("💬 Review for " + item.getProductName());
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidth("400px");

        TextArea input = new TextArea("Your Review");
        input.setWidthFull();
        input.setPlaceholder("Type something meaningful...");
        input.getStyle().set("min-height", "100px");

        Button submit = new Button("Submit", e -> {
            String review = input.getValue();
            if (review == null || review.isBlank()) {
                NotificationView.showError("⚠️ Please enter a review.");
                return;
            }
            try {
                presenter.addReviewToItem(token, item.getStoreId(), item.getProductId(), review);
            } catch (Exception ex) {
                NotificationView.showError("Failed to submit: " + ex.getMessage());
            }
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());

        submit.getStyle()
                .set("background", "linear-gradient(90deg, #10b981, #059669)")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "8px")
                .set("padding", "6px 12px");

        cancel.getStyle()
                .set("background-color", "#e5e7eb")
                .set("color", "#374151")
                .set("border-radius", "8px")
                .set("padding", "6px 12px");

        HorizontalLayout footer = new HorizontalLayout(submit, cancel);
        footer.setSpacing(true);

        content.add(input);
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
                NotificationView.showError("⚠️ Please enter a review.");
                return;
            }
            try {
                presenter.addReviewToStore(token, myStoreId, review);
            } catch (Exception ex) {
                NotificationView.showError("Failed to submit: " + ex.getMessage());
            }
            dialog.close();
        });

        submit.getStyle()
                .set("background", "linear-gradient(90deg, #ce5290, #f472b6)")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "8px")
                .set("padding", "6px 12px")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.1)");

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
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        layout.setWidth("400px");

        TextField quantityField = new TextField("Quantity");
        quantityField.setPlaceholder("Enter quantity (e.g. 1)");
        quantityField.setWidthFull();

        Button submit = new Button("Add", event -> {
            try {
                int quantity = Integer.parseInt(quantityField.getValue());
                if (quantity <= 0) {
                    NotificationView.showError("⚠️ Quantity must be positive.");
                    return;
                }
                presenter.addToCart(token, item, quantity);
                dialog.close();
            } catch (NumberFormatException ex) {
                NotificationView.showError("⚠️ Please enter a valid number.");
            }
        });
        submit.getStyle()
                .set("background", "linear-gradient(90deg, #ce5290, #f472b6)")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "8px")
                .set("padding", "6px 12px")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.1)");

        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.getStyle()
                .set("background-color", "#e5e7eb")
                .set("color", "#374151")
                .set("border-radius", "8px")
                .set("padding", "6px 12px");

        HorizontalLayout buttons = new HorizontalLayout(submit, cancel);
        buttons.setSpacing(true);
        buttons.setJustifyContentMode(JustifyContentMode.END);

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
        priceField.setMin(suggestedPrice);
        priceField.setStepButtonsVisible(true);
        priceField.setValue(suggestedPrice);

        VerticalLayout layout = new VerticalLayout(
                new Paragraph("Current Highest Bid: " + auction.maxBid),
                priceField
        );
        VerticalLayout layout2 = new VerticalLayout(
                new Paragraph("End Date: " + auction.endDate),
                priceField
        );
        dialog.add(layout, layout2);

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
        // Info paragraphs
        Paragraph amountLeftInfo = new Paragraph("Amount Left: " + random.amountLeft);
        long currentTimeMillis = System.currentTimeMillis();
        long timeLeftMillis = random.endTimeMillis - currentTimeMillis;
        long timeLeftMinutes = Math.max(0, timeLeftMillis / (1000 * 60));
        Paragraph timeLeftInfo = new Paragraph("Ending Time: " + timeLeftMinutes + " minutes");
        // Input fields for payment
        TextField cardNumber = new TextField("Card Number");
        TextField cardHolder = new TextField("Cardholder Name");
        TextField expiration = new TextField("Expiration Date (MM/YY)");
        PasswordField cvv = new PasswordField("CVV");

        NumberField amountPaid = new NumberField("Amount Paid");
        amountPaid.setValue(random.productPrice);
        amountPaid.setMin(0.1);

        VerticalLayout form = new VerticalLayout(amountLeftInfo, timeLeftInfo,
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
                List<ItemStoreDTO> items = presenter.searchNormal(token, name, keyword, category, minPrice, maxPrice, productRate, myStoreId);
                if (items == null || items.isEmpty()) {
                    resultsContainer.add(new Paragraph("❌ No normal products found."));
                } else {
                    items.forEach(item -> resultsContainer.add(createItemCard(item)));
                }
                return;
            }

            switch (selectedType) {
                case "Bid" -> {
                    List<BidDTO> bids = presenter.searchBids(token, name, keyword, category, minPrice, maxPrice, productRate, myStoreId);
                    if (bids == null || bids.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No bid products found."));
                    } else {
                        bids.forEach(bid -> resultsContainer.add(createBidCard(bid)));
                    }
                }
                case "Auction" -> {
                    List<AuctionDTO> auctions = presenter.searchAuctions(token, name, keyword, category, minPrice, maxPrice, productRate, myStoreId);
                    if (auctions == null || auctions.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No auction products found."));
                    } else {
                        auctions.forEach(auction -> resultsContainer.add(createAuctionCard(auction)));
                    }
                }
                case "Random Draw" -> {
                    List<RandomDTO> randoms = presenter.searchRandoms(token, name, keyword, category, minPrice, maxPrice, productRate, myStoreId);
                    if (randoms == null || randoms.isEmpty()) {
                        resultsContainer.add(new Paragraph("❌ No random draw products found."));
                    } else {
                        randoms.forEach(random -> resultsContainer.add(createRandomCard(random)));
                    }
                }
                default -> {
                    List<ItemStoreDTO> items = presenter.searchNormal(token, name, keyword, category, minPrice, maxPrice, productRate, myStoreId);
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

        String formattedTime = formatEndTime(auction.endTimeMillis);
        Paragraph endsAt = new Paragraph("⏰ Ends at: " + formattedTime);

        Button makeAuction = new Button("Make Auction", e -> showAuctionBidDialog(auction));
        makeAuction.getStyle()
                .set("background-color", "#2E2E2E")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "8px")
                .set("padding", "6px 16px");

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

        Span name = new Span("🎲 Product Name: " + random.productName);
        Paragraph store = new Paragraph("Store: " + random.storeName);
        Paragraph amountLeft = new Paragraph("Left Amount: " + random.amountLeft);
        Paragraph price = new Paragraph("Price: $" + random.productPrice);

        String formattedEndTime = formatEndTime(random.endTimeMillis);
        Paragraph endTime = new Paragraph("🕒 Ends at: " + formattedEndTime);

        Button participate = new Button("Join Random Draw", e -> showRandomParticipationDialog(random));
        participate.getStyle().set("background-color", "#9c27b0").set("color", "white");

        card.add(name, store, amountLeft, price, endTime, participate);
        return card;
    }

    private String formatEndTime(long endMillis) {
        return java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a", java.util.Locale.ENGLISH)
                .withZone(java.time.ZoneId.systemDefault())
                .format(java.time.Instant.ofEpochMilli(endMillis));
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

    private void openAllAuctionsDialog(List<AuctionDTO> auctions, int productId) {
        Dialog auctionDialog = new Dialog();

        auctionDialog.setHeaderTitle("Available Auctions In This Product");
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);
        mainLayout.setWidthFull();

        for (AuctionDTO auction : auctions) {
            if (auction.status == AuctionStatus.IN_PROGRESS && auction.productId == productId) {
                VerticalLayout auctionDetailsLayout = new VerticalLayout();
                auctionDetailsLayout.setSpacing(false);
                auctionDetailsLayout.setPadding(false);
                auctionDetailsLayout.getStyle().set("line-height", "1.4");
                Paragraph productName = new Paragraph("🛒 Product: " + auction.productName);
                Paragraph highestBid = new Paragraph("💰 Highest Offer: " + auction.maxBid);

                auctionDetailsLayout.add(productName, highestBid);

                Button joinButton = new Button("Join Auction", e -> {
                    auctionDialog.close();
                    showAuctionBidDialog(auction);
                });
                joinButton.setWidth("150px");

                HorizontalLayout row = new HorizontalLayout(auctionDetailsLayout, joinButton);
                row.setWidthFull();
                row.setAlignItems(FlexComponent.Alignment.CENTER);
                row.getStyle().set("justify-content", "space-between");
                row.getStyle().set("border-bottom", "1px solid #e0e0e0");
                row.getStyle().set("padding", "10px 0");

                mainLayout.add(row);
            }
        }
        if (mainLayout.getComponentCount() == 0) {
            mainLayout.add(new Paragraph("No auctions available for this product."));
        }

        auctionDialog.add(mainLayout);
        auctionDialog.setWidth("400px");
        auctionDialog.open();

    }

    private void openAllRandomsDialog(List<RandomDTO> randoms, int productId) {
        Dialog randomDialog = new Dialog();
        randomDialog.setHeaderTitle("Available Random Cards In This Product");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setWidthFull();

        for (RandomDTO random : randoms) {
            if (random.productId == productId) {
                VerticalLayout randomInfoLayout = new VerticalLayout();
                randomInfoLayout.setSpacing(false);
                randomInfoLayout.setPadding(false);
                randomInfoLayout.getStyle().set("line-height", "1.4");
                Paragraph productName = new Paragraph("🛒 Product: " + random.productName);
                Paragraph participationPrice = new Paragraph("💰 Participation Price: " + random.productPrice);

                randomInfoLayout.add(productName, participationPrice);

                Button buyButton = new Button("Buy Random Card", e -> {
                    randomDialog.close();
                    showRandomParticipationDialog(random);
                });

                buyButton.setWidth("150px");

                HorizontalLayout row = new HorizontalLayout(randomInfoLayout, buyButton);
                row.setWidthFull();
                row.setAlignItems(FlexComponent.Alignment.CENTER);
                row.getStyle().set("justify-content", "space-between");
                row.getStyle().set("border-bottom", "1px solid #e0e0e0");
                row.getStyle().set("padding", "10px 0");

                layout.add(row);
            }
        }

        if (layout.getComponentCount() == 0) {
            layout.add(new Paragraph("No random cards available for this product."));
        }

        randomDialog.add(layout);
        randomDialog.setWidth("400px");
        randomDialog.open();
    }

    private void openAllBidsDialog(List<BidDTO> bids, int productId, int storeId) {
        Dialog bidDialog = new Dialog();
        bidDialog.setHeaderTitle("Available Bids In This Product");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setWidthFull();

        for (BidDTO bid : bids) {
            if (bid.productId == productId) {
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();

                String bidDetails = "Product Name: " + bid.productName + "\n Quantity: " + bid.quantity;

                Paragraph bidInfo = new Paragraph(bidDetails);

                Button makeBidButton = new Button("Make a Bid", e -> {
                    bidDialog.close();
                    showBidDialog(bid, storeId);
                });

                row.add(bidInfo, makeBidButton);
                layout.add(row);
            }
        }

        if (layout.getComponentCount() == 0) {
            layout.add(new Paragraph("No bids available for this product."));
        }

        bidDialog.add(layout);
        bidDialog.setWidth("400px");
        bidDialog.open();
    }

    private void openStoreRankDialog() {
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⭐ Rank This Store");
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        layout.setWidth("300px");

        com.vaadin.flow.component.select.Select<Integer> rankSelect = new com.vaadin.flow.component.select.Select<>();
        rankSelect.setLabel("Select a rank");
        rankSelect.setItems(1, 2, 3, 4, 5);
        rankSelect.setPlaceholder("Choose...");
        rankSelect.setWidthFull();

        Button submit = new Button("Submit", e -> {
            Integer rank = rankSelect.getValue();
            if (rank == null) {
                NotificationView.showError("⚠️ Please select a rank.");
                return;
            }
            presenter.rankStore(token, myStoreId, rank);
            NotificationView.showSuccess(" Store ranked successfully!");
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());

        submit.getStyle()
                .set("background", "linear-gradient(90deg, #10b981, #059669)")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "8px")
                .set("padding", "6px 12px");

        cancel.getStyle()
                .set("background-color", "#e5e7eb")
                .set("color", "#374151")
                .set("border-radius", "8px")
                .set("padding", "6px 12px");

        HorizontalLayout buttons = new HorizontalLayout(submit, cancel);
        layout.add(rankSelect, buttons);
        dialog.add(layout);
        dialog.open();
    }
}
