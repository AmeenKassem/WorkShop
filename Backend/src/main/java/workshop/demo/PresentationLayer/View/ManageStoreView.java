package workshop.demo.PresentationLayer.View;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.Presenter.ManageStorePresenter;

@Route(value = "manageStore", layout = MainLayout.class)
public class ManageStoreView extends VerticalLayout implements HasUrlParameter<Integer> {

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

        Button viewEmployeesBtn = new Button("👥 View Employees", e -> showEmployeesDialog(presenter.viewEmployees(myStoreId)));
        Button viewHistoryBtn = new Button("📜 View Store's History", e -> presenter.fetchOrdersByStore(myStoreId));
        Button viewReviewsBtn = new Button("📝 View Store Reviews", e -> presenter.viewStoreReviews(myStoreId));
        Button manageProductsBtn = new Button("🛠 Manage store's products", e -> UI.getCurrent().navigate("manage-store-products/" + myStoreId));
        Button makeOfferBtn = new Button("👤 Manage My Owners", e -> UI.getCurrent().navigate("manageMyOwners/" + myStoreId));
        Button manageManagersBtn = new Button("👔 Manage My Managers", e -> UI.getCurrent().navigate("manage-store-managers/" + myStoreId));
        Button BidBtn = new Button("🎯 Manage Special Purchases", e -> UI.getCurrent().navigate("manage-store-special-purchases/" + myStoreId));
        Button managePolicyBtn = new Button("📋 Manage Store's Policy", e -> new Dialog(new Span("Coming soon!")).open());
        Button deactivateStoreBtn = new Button("📴 Deactivate Store", e -> presenter.deactivateStore(myStoreId));

        List<Button> buttons = List.of(viewEmployeesBtn, viewHistoryBtn, viewReviewsBtn, manageProductsBtn, makeOfferBtn, manageManagersBtn, BidBtn, managePolicyBtn, deactivateStoreBtn);
        buttons.forEach(this::styleManageButton);

        VerticalLayout leftColumn = new VerticalLayout(viewEmployeesBtn, viewHistoryBtn, viewReviewsBtn, manageProductsBtn);
        VerticalLayout rightColumn = new VerticalLayout(makeOfferBtn, manageManagersBtn, BidBtn, managePolicyBtn, deactivateStoreBtn);

        leftColumn.setWidth("48%");
        rightColumn.setWidth("48%");

        HorizontalLayout splitLayout = new HorizontalLayout(leftColumn, rightColumn);
        splitLayout.setWidthFull();
        splitLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        splitLayout.setSpacing(true);
        add(splitLayout);
    }

    private void styleManageButton(Button btn) {
        btn.getStyle()
            .set("background-color", "#ff9900")
            .set("color", "white")
            .set("font-weight", "bold")
            .set("border-radius", "6px")
            .set("padding", "4px 8px")
            .set("font-size", "0.75rem")
            .set("font-family", "'Segoe UI', sans-serif")
            .set("width", "100%")
            .set("margin", "2px 0");
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

    public void showEmployeesDialog(List<WorkerDTO> employees) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("👥 Employees & Roles");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);

        if (employees == null || employees.isEmpty()) {
            content.add(new Paragraph("No employees found in this store."));
        } else {
            for (WorkerDTO emp : employees) {
                VerticalLayout empBlock = new VerticalLayout();
                empBlock.setSpacing(false);
                empBlock.setPadding(false);
                empBlock.getStyle().set("border-bottom", "1px solid #eee").set("padding-bottom", "0.5rem");

                String title = "👤 " + emp.getUsername();

                if (emp.isOwner()) {
                    title += " — Owner";
                    empBlock.add(new Span(title));
                    empBlock.add(new Span("🔒 Fully Authorized"));
                } else if (emp.isManager()) {
                    title += " — Manager";
                    empBlock.add(new Span(title));

                    if (emp.getPermessions() != null && emp.getPermessions().length > 0) {
                        VerticalLayout permsList = new VerticalLayout();
                        permsList.setPadding(false);
                        permsList.setSpacing(false);

                        for (Permission p : emp.getPermessions()) {
                            permsList.add(new Span("• " + p.name()));
                        }

                        empBlock.add(new Span("Authorization:"), permsList);
                    } else {
                        empBlock.add(new Span("🔓 No permissions assigned."));
                    }
                } else {
                    title += " — Employee";
                    empBlock.add(new Span(title));
                }

                content.add(empBlock);
            }
        }

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.getStyle().set("margin-top", "1rem");

        dialog.add(content, closeBtn);
        dialog.open();
    }
}
