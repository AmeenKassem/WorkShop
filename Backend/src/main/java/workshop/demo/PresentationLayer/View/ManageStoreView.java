package workshop.demo.PresentationLayer.View;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.StoreDTO;
import workshop.demo.PresentationLayer.Presenter.ManageStorePresenter;

@Route(value = "manageStore", layout = MainLayout.class)
public class ManageStoreView extends VerticalLayout implements HasUrlParameter<Integer> {

    //private int storeId;
    private ManageStorePresenter presenter;
    private int myStoreId = -1;

    public ManageStoreView() {
        this.presenter = new ManageStorePresenter(this);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer storeId) {
        if (storeId == null) {
            add(new Span("❌ No store ID provided."));
            return;
        }
        System.out.println("🚀 setParameter called with storeId = " + storeId);

        this.myStoreId = storeId;
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            add(new Span("⚠️ You must be logged in to manage your store."));
            return;
        }

        presenter.fetchStore(token, storeId);
    }

    public void buildManageUI(StoreDTO store) {
        removeAll();
        add(new H2("🛍️ " + store.getStoreName()));
        add(new Paragraph("Category: " + store.getCategory()));
        add(new Paragraph("Status: " + (store.isActive() ? "✅ Active" : "❌ Inactive")));
        add(new Paragraph("Rating: " + store.getFinalRating()));

        Button viewEmployeesBtn = new Button("👥 View Employees", e -> Notification.show("Coming soon!"));
        Button changeAuthBtn = new Button("👥 View store's history", e -> presenter.fetchOrdersByStore(myStoreId));
        Button viewReviewsBtn = new Button("📝 View Store Reviews", e -> presenter.viewStoreReviews(myStoreId));
        Button makeOfferBtn = new Button("➕ Manage my owners", e
                -> UI.getCurrent().navigate("manageMyOwners/" + myStoreId));
        Button deleteUserBtn = new Button("➕ Manage my managers", e -> Notification.show("Coming soon!"));
        Button acceptBidBtn = new Button("➕ Accept Bid", e -> Notification.show("Coming soon!"));
        Button deactivateStoreBtn = new Button("📴 Deactivate Store", e -> presenter.deactivateStore(myStoreId));

        add(viewEmployeesBtn, viewReviewsBtn, makeOfferBtn, deleteUserBtn, changeAuthBtn, acceptBidBtn, deactivateStoreBtn);
    }

    public void showDialog(List<String> reviews) {
        Dialog dialog = new Dialog();

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidth("400px");

        if (reviews == null || reviews.isEmpty()) {
            content.add(new Paragraph("There nothing here yet."));
        } else {
            for (String review : reviews) {
                content.add(new Paragraph(review));
            }
        }
        Button closeBtn = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(closeBtn);

        dialog.add(content);
        dialog.open();
    }

}
