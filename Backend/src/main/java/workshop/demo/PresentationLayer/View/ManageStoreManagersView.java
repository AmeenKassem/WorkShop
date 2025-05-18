package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import workshop.demo.DTOs.ManagerDTO;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.PresentationLayer.Layouts.MainLayout;
import workshop.demo.PresentationLayer.Presenter.ManageStoreManagersPresenter;

import java.util.*;

@Route(value = "manage-store-managers/:storeId", layout = MainLayout.class)
@CssImport("./Theme/manageStoreManagersTheme.css")
public class ManageStoreManagersView extends VerticalLayout implements HasUrlParameter<String> {

    private final ManageStoreManagersPresenter presenter;
    private final VerticalLayout managerListContainer = new VerticalLayout();
    private final Button addManagerButton = new Button("Add new manager");
    private final Button backButton = new Button("Back to manager home");

    private String storeId;

    public ManageStoreManagersView() {
        addClassName("manage-store-view");

        this.presenter = new ManageStoreManagersPresenter(this);

        H1 title = new H1("Manage Store Managers");
        title.addClassName("manager-page-title");

        managerListContainer.addClassName("manager-list");

        addManagerButton.addClassName("action-button");
        addManagerButton.addClickListener(e -> {
            VaadinSession.getCurrent().setAttribute("selected-store", storeId);
            UI.getCurrent().navigate("add-manager");
        });

        backButton.addClassName("action-button");
        backButton.addClickListener(e -> UI.getCurrent().navigate("manager-home"));

        HorizontalLayout bottomButtons = new HorizontalLayout(addManagerButton, backButton);
        bottomButtons.addClassName("bottom-button-row");

        VerticalLayout form = new VerticalLayout(title, managerListContainer, bottomButtons);
        form.addClassName("manager-form-container");

        add(form);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String storeId) {
        if (storeId != null) {
            this.storeId = storeId;
            VaadinSession.getCurrent().setAttribute("selected-store", storeId);
            presenter.loadManagers(storeId);
        } else {
            showError("Missing store ID in URL.");
        }
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
                Set<Permission> selected = new HashSet<>();
                checkboxMap.forEach((perm, cb) -> {
                    if (cb.getValue()) selected.add(perm);
                });
                presenter.updateManagerPermissions(getToken(), storeId, manager.getUserId(), selected);
            });

            Button deleteBtn = new Button("Delete", e -> {
                presenter.removeManager(getToken(), storeId, manager.getUserId());
            });

            saveBtn.addClassName("action-button");
            deleteBtn.addClassName("action-button");

            HorizontalLayout actions = new HorizontalLayout(saveBtn, deleteBtn);
            actions.addClassName("card-actions");

            card.add(nameRow, permissionList, actions);
            managerListContainer.add(card);
        }
    }

    private String getToken() {
        return (String) VaadinSession.getCurrent().getAttribute("auth-token");
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
