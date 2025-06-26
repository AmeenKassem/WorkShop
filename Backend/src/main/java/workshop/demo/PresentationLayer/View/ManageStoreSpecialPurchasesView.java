package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
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
public class ManageStoreSpecialPurchasesView extends VerticalLayout implements HasUrlParameter<Integer> {

    private final ManageStoreSpecialPurchasesPresenter presenter;
    private int myStoreId = -1;

    public ManageStoreSpecialPurchasesView() {
        this.presenter = new ManageStoreSpecialPurchasesPresenter(this);

        H1 header = new H1("Manage Store Special Purchases");
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
        // add(new H3("Random Draws"));
        // add(createRandomSection(new RandomDTO[0]));

        add(new H3("Auctions"));
        add(createAuctionSection(auctions));

        add(new H3("Bids"));
        // add(createBidSection(bids));
    }

    private HorizontalLayout createRandomSection(RandomDTO[] randoms) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.getStyle().set("overflow-x", "auto");

        for (RandomDTO dto : randoms) {
            VerticalLayout card = new VerticalLayout();
            card.setWidth("300px");
            card.getStyle().set("border", "1px solid #ccc").set("margin-right", "10px").set("padding", "10px");

            card.add(new Paragraph("Product: " + dto.productName));
            card.add(new Paragraph("Amount Left: $" + dto.amountLeft));
            card.add(new Paragraph("Winner: " + (dto.winner != null ? dto.winner.userId : "Not yet")));

            layout.add(card);
        }

        return layout;
    }

    private HorizontalLayout createAuctionSection(AuctionDTO[] auctions) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.getStyle().set("overflow-x", "auto");

        for (AuctionDTO dto : auctions) {
            VerticalLayout card = new VerticalLayout();
            card.setWidth("300px");
            card.getStyle().set("border", "1px solid #ccc").set("margin-right", "10px").set("padding", "10px");
            card.add(new Paragraph("Product: " + dto.productName));

            // card.add(new Paragraph(
            //         (dto.winnerUserName != null ? dto.winnerUserName + " has win this auction!" : "Not yet")));
            if (dto.status == AuctionStatus.FINISH) {
                if (dto.winnerId != -1) {
                    card.add(new Paragraph(dto.winnerUserName + " has win this auction with bid " + dto.maxBid + "$"));
                } else {
                    card.add(new Paragraph("There is no winner on this bid!!"));
                }
            } else {
                card.add(new Paragraph("Max Bid:" + dto.maxBid + "$"));
                card.add(new Paragraph("The auction will end at " + dto.endDate));
            }
            layout.add(card);
        }

        return layout;
    }

    private HorizontalLayout createBidSection(BidDTO[] bids) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.getStyle().set("overflow-x", "auto").set("white-space", "nowrap");

        for (BidDTO bid : bids) {
            VerticalLayout bidLayout = new VerticalLayout();
            bidLayout.setWidth("300px");
            bidLayout.setPadding(true);
            bidLayout.setSpacing(true);
            bidLayout.getStyle().set("border", "1px solid #ccc").set("margin-right", "10px").set("padding", "10px");

            bidLayout.add(new H3("Bid: " + bid.productName));
            bidLayout.add(new Paragraph("Store: " + bid.storeName));
            bidLayout.add(new Paragraph("Quantity: " + bid.quantity));

            Button showOffers = new Button("Show User Offers");
            HorizontalLayout offersLayout = new HorizontalLayout();
            offersLayout.setVisible(false);
            offersLayout.setWidthFull();
            offersLayout.getStyle().set("overflow-x", "auto").set("white-space", "nowrap");

            showOffers.addClickListener(e -> {
                offersLayout.setVisible(!offersLayout.isVisible());
                showOffers.setText(offersLayout.isVisible() ? "Hide User Offers" : "Show User Offers");
            });

            if (bid.bids != null && bid.bids.length > 0) {
                for (SingleBidDTO offer : bid.bids) {
                    VerticalLayout offerCard = new VerticalLayout();
                    offerCard.setWidth("250px");
                    offerCard.getStyle().set("border", "1px solid #eee").set("padding", "10px").set("margin-right",
                            "10px");

                    offerCard.add(new Paragraph("User ID: " + offer.userId));
                    offerCard.add(new Paragraph("Price: $" + offer.price));
                    offerCard.add(new Paragraph("Product ID: " + offer.productId));
                    // offerCard.add(new Paragraph("Amount: " + offer.amount));
                    // offerCard.add(new Paragraph("Accepted: " + offer.isAccepted));
                    // offerCard.add(new Paragraph("Ended: " + offer.isEnded));

                    if (!offer.isAccepted && !offer.isEnded) {
                        Button acceptBtn = new Button("Accept",
                                ev -> presenter.respondToSingleBid(bid.bidId, offer.id, true));
                        Button rejectBtn = new Button("Reject",
                                ev -> presenter.respondToSingleBid(bid.bidId, offer.id, false));
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
