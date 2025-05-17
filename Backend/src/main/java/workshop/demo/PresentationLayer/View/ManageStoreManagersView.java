package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import workshop.demo.DTOs.ManagerDTO;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.PresentationLayer.Presenter.ManageStoreManagersPresenter;

import java.util.*;

@Route(value = "manage-store-managers", layout = MainLayout.class)
@CssImport("./styles/manage-store-managers.css")
public class ManageStoreManagersView extends VerticalLayout {

    private final ComboBox<String> storeDropdown = new ComboBox<>("Select Store");
    private final VerticalLayout managersLayout = new VerticalLayout();
    private final Map<String, Integer> storeMap = new HashMap<>();

    private final ManageStoreManagersPresenter presenter;

    public ManageStoreManagersView() {
        addClassName("manage-store-managers");
        presenter = new ManageStoreManagersPresenter(this);

        add(new H1("Manage Store Managers"));

        storeDropdown.setPlaceholder("Choose store");
        storeDropdown.addValueChangeListener(event -> {
            String storeName = event.getValue();
            if (storeName != null) {
                int storeId = storeMap.get(storeName);
                presenter.loadManagers(storeId);
            }
        });

        Button addManagerButton = new Button("➕ Add Manager");
        addManagerButton.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigate("add-manager"));
        });

        add(storeDropdown, managersLayout, addManagerButton);

        // Load stores for owner
        presenter.loadOwnedStores();
    }

    public void setStores(Map<String, Integer> stores) {
        storeMap.clear();
        storeMap.putAll(stores);
        storeDropdown.setItems(storeMap.keySet());
    }

    public void displayManagers(List<ManagerDTO> managers) {
        managersLayout.removeAll();

        for (ManagerDTO manager : managers) {
            managersLayout.add(createManagerCard(manager));
        }
    }

    private VerticalLayout createManagerCard(ManagerDTO manager) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle().set("border", "1px solid #ccc");
        card.getStyle().set("padding", "10px");
        card.getStyle().set("margin-bottom", "10px");

        Span title = new Span("👤 " + manager.getManagerName() + " (ID: " + manager.getManagerId() + ")");
        card.add(title);

        Set<Permission> selected = new HashSet<>(manager.getPermissions());
        Map<Permission, Button> buttons = new HashMap<>();

        for (Permission p : Permission.values()) {
            Button toggle = new Button(selected.contains(p) ? "✅ " + p.name() : "❌ " + p.name());
            toggle.addClickListener(e -> {
                if (selected.contains(p)) {
                    selected.remove(p);
                    toggle.setText("❌ " + p.name());
                } else {
                    selected.add(p);
                    toggle.setText("✅ " + p.name());
                }
            });
            buttons.put(p, toggle);
            card.add(toggle);
        }

        HorizontalLayout actions = new HorizontalLayout();

        Button saveBtn = new Button("💾 Save");
        saveBtn.addClickListener(e -> {
            manager.setPermissions(selected);
            presenter.updatePermissions(manager);
        });

        Button removeBtn = new Button("🗑 Remove");
        removeBtn.addClickListener(e -> {
            presenter.removeManager(manager.getStoreId(), manager.getManagerId());
        });

        actions.add(saveBtn, removeBtn);
        card.add(actions);

        return card;
    }

    public void showSuccess(String msg) {
        Notification.show("✅ " + msg, 3000, Notification.Position.MIDDLE);
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg, 4000, Notification.Position.MIDDLE);
    }

    public void refresh() {
        String selected = storeDropdown.getValue();
        if (selected != null) {
            presenter.loadManagers(storeMap.get(selected));
        }
    }
}