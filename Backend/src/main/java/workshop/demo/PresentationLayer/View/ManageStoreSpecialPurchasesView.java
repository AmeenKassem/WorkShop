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

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.AuctionStatus;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SingleBidDTO;
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
            card.add(new Paragraph("🏬 Store: " + dto.storeName));
            card.add(new Paragraph("📦 Quantity: " + dto.quantity));
            card.add(new Paragraph("🔢 Amount Left: " + dto.amountLeft));
            card.add(new Paragraph("💵 Price: $" + dto.productPrice));

            long now = System.currentTimeMillis();
            if (now >= dto.endTimeMillis) {
                if (dto.userName != null) {
                    card.add(new Paragraph("🏆 Winner: " + dto.userName));
                } else {
                    card.add(new Paragraph("❌ No winner"));
                }
                card.add(new Paragraph("⏰ Ended at: " + dto.endDate));
            } else {
                long secondsLeft = (dto.endTimeMillis - now) / 1000;
                long minutes = secondsLeft / 60;
                long seconds = secondsLeft % 60;
                card.add(new Paragraph("⌛ Ends in: " + minutes + "m " + seconds + "s"));
            }

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
            card.add(new Paragraph("📦 Quantity: " + dto.quantity));

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
        VerticalLayout card = new VerticalLayout();
        card.addClassName("special-card");

        card.add(new Paragraph("🛍️ Product: " + bid.productName));
        card.add(new Paragraph("🏬 Store: " + bid.storeName));
        card.add(new Paragraph("📦 Quantity: " + bid.quantity));

        Button showOffers = new Button("📜 Show User Offers");
        showOffers.getStyle().set("margin-top", "0.5rem");

        VerticalLayout offersLayout = new VerticalLayout();
        offersLayout.setVisible(false);
        offersLayout.setPadding(false);
        offersLayout.setSpacing(true);
        offersLayout.addClassName("scroll-section");

        showOffers.addClickListener(e -> {
            offersLayout.setVisible(!offersLayout.isVisible());
            showOffers.setText(offersLayout.isVisible() ? "🔽 Hide User Offers" : "📜 Show User Offers");
        });

        if (bid.bids != null && bid.bids.length > 0) {
            for (SingleBidDTO offer : bid.bids) {
                VerticalLayout offerCard = new VerticalLayout();
                offerCard.addClassName("offer-card");
                offerCard.getStyle()
                        .set("background-color", "#fdf2f8")
                        .set("border", "1px solid #f9a8d4")
                        .set("border-radius", "10px")
                        .set("padding", "0.8rem")
                        .set("margin-bottom", "0.5rem")
                        .set("box-shadow", "0 2px 6px rgba(0,0,0,0.05)");

                offerCard.add(new Paragraph("👤 UserName: " + offer.userName));
                offerCard.add(new Paragraph("💵 Price: $" + offer.price));
                offerCard.add(new Paragraph("🧾 Quantity: " + offer.amount));
                offerCard.add(new Paragraph("🆔 Product ID: " + offer.productId));

                if (!offer.isAccepted && !offer.isEnded) {
                    Button acceptBtn = new Button("✅ Accept", ev -> presenter.respondToSingleBid(bid.bidId, offer.id, true));
                    Button rejectBtn = new Button("❌ Reject", ev -> presenter.respondToSingleBid(bid.bidId, offer.id, false));

                    acceptBtn.addClassName("v-button");
                    rejectBtn.addClassName("v-button");

                    HorizontalLayout buttons = new HorizontalLayout(acceptBtn, rejectBtn);
                    buttons.setSpacing(true);
                    offerCard.add(buttons);
                } else {
                    offerCard.add(new Paragraph(offer.isAccepted ? "✅ Accepted" : "❌ Rejected or Ended"));
                }

                offersLayout.add(offerCard);
            }
        } else {
            offersLayout.add(new Paragraph("No user offers yet."));
        }

        card.add(showOffers, offersLayout);
        layout.add(card);
    }

    return layout;
}



    public void refreshPage() {
        UI.getCurrent().getPage().reload();
    }
}
