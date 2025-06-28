package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import workshop.demo.DTOs.*;
import workshop.demo.PresentationLayer.Presenter.ManageStoreSpecialPurchasesPresenter;

@Route(value = "manage-store-special-purchases", layout = MainLayout.class)
@CssImport("./Theme/managespecialPurchase.css")
public class ManageStoreSpecialPurchasesView extends VerticalLayout implements HasUrlParameter<Integer> {

    private final ManageStoreSpecialPurchasesPresenter presenter;
    private int myStoreId = -1;

    public ManageStoreSpecialPurchasesView() {
        this.presenter = new ManageStoreSpecialPurchasesPresenter(this);
        H1 header = new H1("🛍️ Special Purchase Management");
        header.addClassName("main-header");
        header.getStyle().set("margin", "0 auto");

        addClassName("admin-panel-wrapper");
        add(header);

    }

    @Override
    public void setParameter(BeforeEvent event, Integer storeId) {
        if (storeId == null) {
            add(new Paragraph("❌ No store ID provided."));
            return;
        }
        this.myStoreId = storeId;
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            add(new Paragraph("⚠️ You must be logged in to manage your store's special purchases."));
            return;
        }
        presenter.setStoreId(storeId);
    }

    public void showAllSpecials(RandomDTO[] randoms, AuctionDTO[] auctions, BidDTO[] bids) {
        add(new H3("Random Draws"));
        add(createRandomSection(randoms));

        H3 auctionHeader = new H3("📣 Auctions");
        auctionHeader.addClassName("section-header");
        add(auctionHeader);
        add(createAuctionSection(auctions));

        H3 randomHeader = new H3("🎲 Random Draws");
        randomHeader.addClassName("section-header");
        add(randomHeader);
        add(createRandomSection(randoms));

        H3 bidsHeader = new H3("🤝 Bids");
        bidsHeader.addClassName("section-header");
        add(bidsHeader);
        add(createBidSection(bids));
    }

   private HorizontalLayout createRandomSection(RandomDTO[] randoms) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.addClassName("scroll-section");
        if (randoms == null || randoms.length == 0) {
            layout.add(new Paragraph("No random draws available."));
            return layout;
        }
        for (RandomDTO dto : randoms) {
            VerticalLayout card = new VerticalLayout();
            card.addClassName("special-card");

            card.add(new Paragraph("🎁 Product: " + dto.productName));
            card.add(new Paragraph("🔢 Amount Left: " + dto.amountLeft));
            card.add(new Paragraph("🏆 Winner: " + (dto.winner != null ? dto.winner.userId : "Not yet")));

            layout.add(card);
        }

        return layout;
    }

   private HorizontalLayout createAuctionSection(AuctionDTO[] auctions) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.addClassName("scroll-section");
        if (auctions == null || auctions.length == 0) {
            layout.add(new Paragraph("No auctions available."));
            return layout;
        }
            for (AuctionDTO dto : auctions) {
            VerticalLayout card = new VerticalLayout();
            card.addClassName("special-card");

            card.add(new Paragraph("🛒 Product: " + dto.productName));

            if (dto.status == AuctionStatus.FINISH) {
                if (dto.winnerId != -1) {
                    card.add(new Paragraph("🏆 Winner: " + dto.winnerUserName + " with $" + dto.maxBid));
                    card.add(new Paragraph("⏰ Ended at: " + dto.endDate));
                } else {
                    card.add(new Paragraph("❌ No winner for this auction."));
                    card.add(new Paragraph("⏰ Ended at: " + dto.endDate));
                }
            } else {
                card.add(new Paragraph("📈 Max Bid: $" + dto.maxBid));
                card.add(new Paragraph("⏰ Ends at: " + dto.endDate));
            }

            layout.add(card);
        }

        return layout;
    }
   private HorizontalLayout createBidSection(BidDTO[] bids) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.addClassName("scroll-section");
        if (bids == null || bids.length == 0) {
            layout.add(new Paragraph("No bids available."));
            return layout;
        }

        for (BidDTO bid : bids) {
            VerticalLayout bidLayout = new VerticalLayout();
            bidLayout.addClassName("special-card");

            bidLayout.add(new Paragraph("Bid: " + bid.productName));
            bidLayout.add(new Paragraph("Store: " + bid.storeName));
            bidLayout.add(new Paragraph("Quantity: " + bid.quantity));

            Button showOffers = new Button("Show User Offers");
            HorizontalLayout offersLayout = new HorizontalLayout();
            offersLayout.setVisible(false);
            offersLayout.addClassName("scroll-section");

            showOffers.addClickListener(e -> {
                offersLayout.setVisible(!offersLayout.isVisible());
                showOffers.setText(offersLayout.isVisible() ? "Hide User Offers" : "Show User Offers");
            });

            if (bid.bids != null && bid.bids.length > 0) {
                for (SingleBidDTO offer : bid.bids) {
                    VerticalLayout offerCard = new VerticalLayout();
                    offerCard.addClassName("offer-card");

                    offerCard.add(new Paragraph("User ID: " + offer.userId));
                    offerCard.add(new Paragraph("Price: $" + offer.price));
                    offerCard.add(new Paragraph("Product ID: " + offer.productId));

                    if (!offer.isAccepted && !offer.isEnded) {
                        Button acceptBtn = new Button("Accept", ev -> presenter.respondToSingleBid(bid.bidId, offer.id, true));
                        Button rejectBtn = new Button("Reject", ev -> presenter.respondToSingleBid(bid.bidId, offer.id, false));
                        HorizontalLayout buttons = new HorizontalLayout(acceptBtn, rejectBtn);
                        offerCard.add(buttons);
                    }

                    offersLayout.add(offerCard);
                }
            } else {
                offersLayout.add(new Paragraph("No user offers yet."));
            }

            bidLayout.add(showOffers, offersLayout);
            layout.add(bidLayout);
        }

        return layout;
    }


    public void refreshPage() {
        UI.getCurrent().getPage().reload();
    }
}
