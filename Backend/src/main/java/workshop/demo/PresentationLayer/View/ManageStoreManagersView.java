package workshop.demo.PresentationLayer.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.ManagerDTO;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.PresentationLayer.Presenter.ManageStoreManagersPresenter;

@Route(value = "manage-store-managers/:storeId", layout = MainLayout.class)
@CssImport("./Theme/manageStoreManagersTheme.css")
public class ManageStoreManagersView extends VerticalLayout implements HasUrlParameter<Integer> {
    private final ManageStoreManagersPresenter presenter;
    private final VerticalLayout managerListContainer = new VerticalLayout();
    private final Button addManagerButton = new Button("Add new manager");
    private final Button backButton = new Button("Back to manager home");

    public ManageStoreManagersView() {
        addClassName("manage-store-view");

        this.presenter = new ManageStoreManagersPresenter(this);

        H1 title = new H1("Manage Store Managers");
        title.addClassName("manager-page-title");

        managerListContainer.addClassName("manager-list");

        addManagerButton.addClassName("add-manager-button");
        addManagerButton.addClickListener(e -> presenter.addManager());

        backButton.addClassName("back-to-manager-home-button");
        backButton.addClickListener(e -> presenter.back());

        HorizontalLayout bottomButtons = new HorizontalLayout(addManagerButton, backButton);
        bottomButtons.addClassName("bottom-button-row");

        VerticalLayout form = new VerticalLayout(title, managerListContainer, bottomButtons);
        form.addClassName("manager-form-container");

        add(form);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer storeId) {
        if (storeId == null) {
            showError("❌ No store ID provided.");
            return;
        }

        System.out.println("🚀 setParameter called with storeId = " + storeId);
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            showError("⚠️ You must be logged in to manage your store.");
            return;
        }

        presenter.setStoreId(String.valueOf(storeId));
    }

    public void updateManagerList(List<ManagerDTO> managers) {
        managerListContainer.removeAll();

        for (ManagerDTO manager : managers) {
            VerticalLayout card = new VerticalLayout();
            card.addClassName("manager-card");

            HorizontalLayout nameRow = new HorizontalLayout();
            nameRow.addClassName("manager-card-header");

            Span nameLabel = new Span("Manager name:");
            Span nameValue = new Span(manager.getUsername());
            nameValue.addClassName("manager-username");

            nameRow.add(nameLabel, nameValue);

            VerticalLayout permissionList = new VerticalLayout();
            permissionList.addClassName("permission-list");
            Map<Permission, Checkbox> checkboxMap = new HashMap<>();
            for (Permission p : Permission.values()) {
                Checkbox cb = new Checkbox(getPermissionLabel(p));
                cb.setValue(manager.getPermissions().contains(p));
                checkboxMap.put(p, cb);
                permissionList.add(cb);
            }

            Button saveBtn = new Button("Save changes", e -> {
                presenter.savePermissions(manager.getUserId(), checkboxMap);
            });
            saveBtn.addClassName("save-permissions-button");
            Button deleteBtn = new Button("Delete", e -> {
                presenter.deleteManager(manager.getUserId());
            });
            deleteBtn.addClassName("delete-manager-button");

            HorizontalLayout actions = new HorizontalLayout(saveBtn, deleteBtn);
            actions.addClassName("card-actions");

            card.add(nameRow, permissionList, actions);
            managerListContainer.add(card);
        }
    }

    private String getPermissionLabel(Permission p) {
        return switch (p) {
            case ViewAllProducts -> "View all products";
            case AddToStock -> "Add new Product to the store";
            case DeleteFromStock -> "Delete existing product from the store";
            case UpdateQuantity -> "Handle customer complaints";
            case UpdatePrice -> "Change the price of products";
            case SpecialType -> "Manage other managers";
        };
    }

    public void showSuccess(String msg) {
        Notification.show("✅ " + msg, 3000, Notification.Position.BOTTOM_CENTER);
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg, 5000, Notification.Position.BOTTOM_CENTER);
    }
}
