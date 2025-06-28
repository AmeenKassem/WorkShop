package workshop.demo.PresentationLayer.View;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
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
@CssImport("./Theme/manageStore.css")
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
        H2 title = new H2("🛍️ " + store.getStoreName());
        title.addClassName("store-title");
        title.setWidthFull();
        setAlignItems(Alignment.CENTER);
        add(title);

        List<Button> buttons = List.of(
                new Button("👥 View Employees", e -> showEmployeesDialog(presenter.viewEmployees(myStoreId))),
                new Button("📜 View Store's History", e -> presenter.fetchOrdersByStore(myStoreId)),
                new Button("📝 View Store Reviews", e -> presenter.viewStoreReviews(myStoreId)),
                new Button("🛠 Manage store's products", e -> UI.getCurrent().navigate("manage-store-products/" + myStoreId)),
                new Button("👤 Manage My Owners", e -> UI.getCurrent().navigate("manageMyOwners/" + myStoreId)),
                new Button("👔 Manage My Managers", e -> UI.getCurrent().navigate("manage-store-managers/" + myStoreId)),
                new Button("🎯 Manage Special Purchases", e -> UI.getCurrent().navigate("manage-store-special-purchases/" + myStoreId)),
                new Button("📋 Manage Store's Policy", e -> openPurchasePolicyDialog()),
                new Button("📴 Deactivate Store", e -> presenter.deactivateStore(myStoreId))
        );

        FlexLayout buttonContainer = new FlexLayout();
        buttonContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        buttonContainer.setJustifyContentMode(FlexLayout.JustifyContentMode.CENTER);
        buttonContainer.addClassName("manage-button-container");

        buttons.forEach(btn -> {
            btn.addClassName("manage-button");
            buttonContainer.add(btn);
        });

        add(buttonContainer);
    }

    public void showDialog(List<String> reviews) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⭐ Store Reviews");

<<<<<<< HEAD
    VerticalLayout content = new VerticalLayout();
    content.addClassName("dialog-content");
    content.setSpacing(true);
    content.setPadding(true);
    content.setWidth("400px");
    content.setMaxHeight("400px");
    content.getStyle().set("overflow", "auto");
=======
        VerticalLayout content = new VerticalLayout();
        content.addClassName("dialog-content");
        content.setSpacing(true);
        content.setPadding(true);
        content.setWidth("400px");
>>>>>>> persist-auth

        if (reviews == null || reviews.isEmpty()) {
            content.add(new Paragraph("Nothing here yet."));
        } else {
            for (String review : reviews) {
                String name = extractValue(review, "name");
                String msg = extractValue(review, "reviewMsg");

                VerticalLayout reviewBox = new VerticalLayout();
                reviewBox.getStyle()
                        .set("background-color", "white")
                        .set("border-radius", "10px")
                        .set("padding", "0.8rem")
                        .set("box-shadow", "0 2px 6px rgba(0,0,0,0.1)");
                reviewBox.setSpacing(false);
                reviewBox.setPadding(false);

                Span nameSpan = new Span("👤 " + name);
                nameSpan.getStyle().set("font-weight", "bold");

                Span msgSpan = new Span("💬 " + msg);

                reviewBox.add(nameSpan, msgSpan);
                content.add(reviewBox);
            }
        }

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.addClassName("dialog-close-button");

        dialog.add(content);
        dialog.getFooter().add(closeBtn);
        dialog.open();
    }

    private String extractValue(String raw, String key) {
        try {
            int start = raw.indexOf(key + "=");
            if (start == -1) {
                return "";
            }
            int end = raw.indexOf(",", start + key.length() + 1);
            if (end == -1) {
                end = raw.indexOf("}", start);
            }
            return raw.substring(start + key.length() + 1, end).trim();
        } catch (Exception e) {
            return "";
        }
    }

    public void showEmployeesDialog(List<WorkerDTO> employees) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("👥 Employees & Roles");

        VerticalLayout content = new VerticalLayout();
        content.addClassName("dialog-content");
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

        content.add(closeBtn);
        dialog.add(content);
        dialog.open();
    }

    /* ──────────────────────────────────────────────────────────────
     *  Purchase-Policy dialog
     * ─────────────────────────────────────────────────────────────*/
 /* ───────────────────────────────────────────────────────────────
     *  Purchase-Policy dialog  (add / remove)
     * ─────────────────────────────────────────────────────────────── */
    private void openPurchasePolicyDialog() {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Add / Remove Purchase Policies");

        ComboBox<String> keyBox = new ComboBox<>("Policy");
        keyBox.setItems("NO_ALCOHOL", "MIN_QTY");
        keyBox.setItemLabelGenerator(k -> switch (k) {
            case "NO_ALCOHOL" ->
                "No alcohol under 18";
            case "MIN_QTY" ->
                "Minimum quantity per product";
            default ->
                k;
        });
        keyBox.setValue("NO_ALCOHOL");

        NumberField paramField = new NumberField("Minimum quantity");
        paramField.setMin(1);
        paramField.setStepButtonsVisible(true);
        paramField.setValue(1.0);
        paramField.setVisible(false);

        keyBox.addValueChangeListener(ev
                -> paramField.setVisible("MIN_QTY".equals(ev.getValue())));

        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");

        Button add = new Button("Add", e -> {
            try {
                Integer p = paramField.isVisible() ? paramField.getValue().intValue() : null;
                presenter.addPurchasePolicy(myStoreId, token, keyBox.getValue(), p);
                NotificationView.showSuccess("Policy added");
                dlg.close();
            } catch (Exception ex) {
                ExceptionHandlers.handleException(ex);
            }
        });
        add.addClassNames("dialog-button", "confirm");

        Button remove = new Button("Remove", e -> {
            try {
                Integer p = paramField.isVisible() ? paramField.getValue().intValue() : null;
                presenter.removePurchasePolicy(myStoreId, token, keyBox.getValue(), p);
                NotificationView.showSuccess("Policy removed");
                dlg.close();
            } catch (Exception ex) {
                ExceptionHandlers.handleException(ex);
            }
        });
        remove.addClassNames("dialog-button", "confirm");

        Button cancel = new Button("Cancel", e -> dlg.close());
        cancel.addClassNames("dialog-button", "cancel");

        VerticalLayout content = new VerticalLayout();
        content.addClassName("dialog-content");
        content.add(keyBox, paramField, new HorizontalLayout(add, remove, cancel));

        dlg.add(content);
        dlg.open();
    }

<<<<<<< HEAD


   public void showStoreOrdersDialog(List<String> orders) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("📜 Store's Order History");

        VerticalLayout content = new VerticalLayout();
        content.addClassName("dialog-content");
        content.setSpacing(true);
        content.setPadding(true);
        content.setWidth("500px");

        content.setMaxHeight("400px");
        content.getStyle().set("overflow", "auto");

        if (orders == null || orders.isEmpty()) {
            content.add(new Paragraph("There are no orders yet."));
        } else {
            for (String order : orders) {
                VerticalLayout card = new VerticalLayout();
                card.getStyle()
                    .set("background-color", "#fff")
                    .set("border", "1px solid #ddd")
                    .set("border-radius", "10px")
                    .set("padding", "1rem")
                    .set("margin-bottom", "1rem")
                    .set("box-shadow", "0 2px 6px rgba(0,0,0,0.08)");
                card.setSpacing(false);
                card.setPadding(false);

                Span orderDetails = new Span(order);
                orderDetails.getStyle().set("white-space", "pre-line");
                card.add(orderDetails);
                content.add(card);
            }
        }

        Button close = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(close);
        dialog.add(content);
        dialog.open();
    }
=======
>>>>>>> persist-auth
}
