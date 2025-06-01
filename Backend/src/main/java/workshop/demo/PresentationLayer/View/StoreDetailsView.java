package workshop.demo.PresentationLayer.View;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.ReviewDTO;
import workshop.demo.PresentationLayer.Presenter.StoreDetailsPresenter;

@Route(value = "store", layout = MainLayout.class)
public class StoreDetailsView extends VerticalLayout implements HasUrlParameter<Integer> {

    private int myStoreId;
    private final FlexLayout productContainer = new FlexLayout();
    private final StoreDetailsPresenter presenter;

    public StoreDetailsView() {
        this.presenter = new StoreDetailsPresenter();
        add(new H1("Store Details "));

        // Store-level buttons
        HorizontalLayout storeActions = new HorizontalLayout();
        storeActions.setSpacing(true);

        Button showReviewsBtn = new Button("📖 Show Store Reviews", e -> showStoreReviewsDialog());
        Button addReviewBtn = new Button("📝 Add Review to Store", e -> openStoreReviewDialog());

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
        List<RandomDTO> randomProductIds = presenter.getRandomProductIds(myStoreId, token);
        List<AuctionDTO> auctionProductIds = presenter.getAuctionProductIds(myStoreId, token);

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
            Button randomButton = new Button("🎲 Buy Random Card", e -> {
                NotificationView.showSuccess("🎉 Coming soon: Random card draw!");
            });
            randomButton.setWidthFull();
            actions.add(randomButton);
        }
        //bid:

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

}
