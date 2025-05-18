package workshop.demo.PresentationLayer.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.ManagerDTO;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.PresentationLayer.Presenter.ManageStoreManagersPresenter;

@Route("manage-store-managers")
public class ManageStoreManagersView extends VerticalLayout {

    private final ManageStoreManagersPresenter presenter;
    private ComboBox<String> storeSelector;
    private VerticalLayout managerListContainer;

    public ManageStoreManagersView() {
        this.presenter = new ManageStoreManagersPresenter(this);
        setupLayout();
        presenter.loadStores();
    }

    private void setupLayout() {
        setSpacing(true);
        H1 title = new H1("manage store manager");

        storeSelector = new ComboBox<>("select store :");
        storeSelector.addValueChangeListener(e -> {
            String storeId = e.getValue();
            if (storeId != null) {
                presenter.loadManagers(storeId);
            }
        });

        Button refreshBtn = new Button("refresh", e -> {
            String storeId = storeSelector.getValue();
            if (storeId != null) {
                presenter.loadManagers(storeId);
            }
        });

        HorizontalLayout topBar = new HorizontalLayout(storeSelector, refreshBtn);
        managerListContainer = new VerticalLayout();

        //  move to the add manager page 
        Button addManagerBtn = new Button("Add new manager", e ->
                getUI().ifPresent(ui -> ui.navigate("add-manager"))
        );

        // manager home page 
        Button backBtn = new Button("back to manager home", e ->
                getUI().ifPresent(ui -> ui.navigate("manager-home"))
        );

        add(title, topBar, managerListContainer, addManagerBtn, backBtn);
    }

    public void populateStores(List<String> stores) {
        storeSelector.setItems(stores);
    }

    public void updateManagerGrid(List<ManagerDTO> managers) {
        managerListContainer.removeAll();

        for (ManagerDTO manager : managers) {
            VerticalLayout card = new VerticalLayout();
            card.getStyle().set("background-color", "#d9d9d9").set("padding", "1rem");

            Span name = new Span("manager name : " + manager.getUsername());
            name.getStyle().set("font-weight", "bold");

            List<Checkbox> checkboxes = new ArrayList<>();
            for (Permission perm : Permission.values()) {
                Checkbox cb = new Checkbox(getPermissionLabel(perm));
                cb.setValue(manager.getPermissions().contains(perm));
                checkboxes.add(cb);
            }

            Button save = new Button("save changes", e -> {
                Set<Permission> selected = checkboxes.stream()
                        .filter(Checkbox::getValue)
                        .map(cb -> parsePermission(cb.getLabel()))
                        .collect(Collectors.toSet());
                presenter.updateManagerPermissions(getToken(), storeSelector.getValue(), manager.getUserId(), selected);
            });

            Button delete = new Button("delete", e -> {
                presenter.removeManager(getToken(), storeSelector.getValue(), manager.getUserId());
            });

            HorizontalLayout actions = new HorizontalLayout(save, delete);
            card.add(name);
            checkboxes.forEach(card::add);
            card.add(actions);
            managerListContainer.add(card);
        }
    }

    private Permission parsePermission(String label) {
        return Arrays.stream(Permission.values())
                .filter(p -> getPermissionLabel(p).equals(label))
                .findFirst().orElse(Permission.ViewAllProducts);
    }

    private String getPermissionLabel(Permission p) {
        return switch (p) {
            case ViewAllProducts -> "View all products";
            case AddToStock -> "Add new Product to the store";
            case DeleteFromStock -> "Delete existing product from the store";
            case UpdateQuantity -> "Update quantity";
            case UpdatePrice -> "Change the price of products";
            case SpecialType -> "Manage other managers";
        };
    }

    public void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.BOTTOM_CENTER);
    }

    public void showError(String message) {
        Notification.show("Error: " + message, 4000, Notification.Position.BOTTOM_CENTER);
    }

    private String getToken() {
        return (String) VaadinSession.getCurrent().getAttribute("auth-token");
    }
}
