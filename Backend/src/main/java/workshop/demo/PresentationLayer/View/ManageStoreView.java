package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
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

@Route(value = "manageStore/:id", layout = MainLayout.class)
public class ManageStoreView extends VerticalLayout implements HasUrlParameter<Integer> {

    //private int storeId;
    private ManageStorePresenter presenter;

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

        //this.storeId = storeIdTemp;
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
        Button viewReviewsBtn = new Button("📝 View Reviews", e -> Notification.show("Coming soon!"));
        Button makeOfferBtn = new Button("➕ Make Offer to Add", e -> Notification.show("Coming soon!"));
        Button deleteUserBtn = new Button("❌ Remove Owner/Manager", e -> Notification.show("Coming soon!"));
        Button changeAuthBtn = new Button("🔧 Change Manager Permissions", e -> Notification.show("Coming soon!"));
        Button deactivateStoreBtn = new Button("📴 Deactivate Store", e -> Notification.show("Coming soon!"));

        add(viewEmployeesBtn, viewReviewsBtn, makeOfferBtn, deleteUserBtn, changeAuthBtn, deactivateStoreBtn);
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg, 5000, Notification.Position.BOTTOM_CENTER);
    }
}
