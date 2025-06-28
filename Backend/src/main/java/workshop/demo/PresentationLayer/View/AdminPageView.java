package workshop.demo.PresentationLayer.View;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DTOs.UserSuspensionDTO;
import workshop.demo.PresentationLayer.Presenter.AdminPagePresenter;

@Route(value = "admin", layout = MainLayout.class)
@CssImport("./Theme/adminPage.css")
public class AdminPageView extends VerticalLayout {

    private final AdminPagePresenter presenter;

    public AdminPageView() {
        presenter = new AdminPagePresenter(this);
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        H2 title = new H2("🔧 Let’s Manage the System!");
        title.addClassName("main-header"); 
        title.getStyle().set("margin", "0 auto");
        add(title);

        Button viewHistoryBtn = new Button("📊 View Purchase History", e -> UI.getCurrent().navigate("admin-purchase-history"));

        Button manageUsersBtn = new Button("👥 Manage Users", e -> showManageUsers());
        Button manageStoresBtn = new Button("👥 Manage Stores", e -> showManageStores());
        Button shutdownSystemBtn = new Button("❌ Shutdown System", e -> showShutdownConfirmation());
        VerticalLayout actionButtons = new VerticalLayout(viewHistoryBtn, manageUsersBtn, manageStoresBtn, shutdownSystemBtn);
        actionButtons.setClassName("admin-panel-card");
        addClassName("admin-panel-wrapper");
        actionButtons.setAlignItems(Alignment.STRETCH);
        add(actionButtons);
    }

    private void showManageUsers() {
    removeAll(); // Clear previous content

    H2 header = new H2("🔒 User Suspension Management");
    header.addClassName("main-header");

    Button viewSuspensionsBtn = new Button("👁️ View Suspensions", e -> showSuspensionsDialog());
    viewSuspensionsBtn.addClassName("v-button");
    viewSuspensionsBtn.addClassName("secondary");

    Grid<UserDTO> userGrid = new Grid<>(UserDTO.class, false);
    userGrid.addClassName("v-grid");
    userGrid.setWidthFull();
    userGrid.setWidth("100%");
    userGrid.setAllRowsVisible(true);

    userGrid.addColumn(user -> user.username != null ? user.username : "Guest").setHeader("Username");
    userGrid.addColumn(user -> user.age).setHeader("Age");
    userGrid.addColumn(user -> Boolean.TRUE.equals(user.isOnline) ? "Online" : "Offline").setHeader("Status");
    userGrid.addColumn(user -> Boolean.TRUE.equals(user.isAdmin) ? "Admin" : "User").setHeader("Role");

    userGrid.addComponentColumn(user -> {
        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("actions-wrapper");

        Button suspend = new Button("⏸ Suspend", click -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Suspend User");

            TextField minutesField = new TextField("Suspend for (minutes)");
            minutesField.setPlaceholder("e.g., 10");
            minutesField.setWidthFull();

            Button confirm = new Button("✅ OK", e -> {
                try {
                    int minutes = Integer.parseInt(minutesField.getValue());
                    presenter.onSuspendUser(user.id, minutes);
                    dialog.close();
                } catch (NumberFormatException ex) {
                    NotificationView.showError("Invalid input");
                }
            });

            confirm.addClassName("v-button");
            confirm.addClassName("danger");

            VerticalLayout dialogLayout = new VerticalLayout(minutesField, confirm);
            dialogLayout.setSpacing(true);
            dialogLayout.setPadding(true);
            dialog.add(dialogLayout);
            dialog.open();
        });

        Button pause = new Button("⏸ Pause", click -> presenter.onPauseSuspension(user.getId()));
        Button resume = new Button("▶️ Resume", click -> presenter.onResumeSuspension(user.getId()));
        Button cancel = new Button("❌ Cancel", click -> presenter.onCancelSuspension(user.getId()));


        actions.add(suspend, pause, resume, cancel);
        return actions;
    }).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);

    try {
        List<UserDTO> users = presenter.getAllUsers();
        List<UserDTO> nonAdminUsers = users.stream()
                .filter(user -> !Boolean.TRUE.equals(user.isAdmin))
                .toList();
        userGrid.setItems(nonAdminUsers);
    } catch (Exception e) {
        add(new Paragraph("❌ Failed to load users: " + e.getMessage()));
        return;
    }

    VerticalLayout userPanel = new VerticalLayout(header, viewSuspensionsBtn, userGrid);
    userPanel.addClassName("admin-panel-card");
    userPanel.setWidthFull();

    addClassName("admin-panel-wrapper");
    add(userPanel);
}

      private void showSuspensionsDialog() {
        List<UserSuspensionDTO> suspensions = presenter.onViewSuspensions();

        Dialog dialog = new Dialog();
        dialog.getElement().getClassList().add("dialog-content");
        dialog.setHeaderTitle("🚫 Suspended Users");
        dialog.setWidth("850px");
        dialog.setHeight("480px");

        if (suspensions.isEmpty()) {
            Paragraph emptyMsg = new Paragraph("No suspended users.");
            emptyMsg.getStyle().set("padding", "1rem").set("color", "#6b7280");
            dialog.add(emptyMsg);
        } else {
            Grid<UserSuspensionDTO> grid = new Grid<>(UserSuspensionDTO.class, false);

            grid.addColumn(UserSuspensionDTO::getUserName).setHeader("User Name");
            grid.addColumn(s -> s.isPaused() ? "Yes" : "No").setHeader("Paused");
            grid.addColumn(s -> s.getSuspensionEndTime() != null ? s.getSuspensionEndTime().toString() : "N/A")
                .setHeader("End Time").setAutoWidth(true).setFlexGrow(0);
            grid.addColumn(UserSuspensionDTO::getRemainingWhenPaused)
                .setHeader("Remaining Time").setAutoWidth(true).setFlexGrow(0);

            grid.setItems(suspensions);
            grid.setWidthFull();
            grid.setHeight("370px");
            grid.addClassName("v-grid");
            grid.getStyle().set("font-size", "0.95rem").set("background", "#fff");
            dialog.add(grid);
        }

        Button close = new Button("Close", e -> dialog.close());
        close.addClassName("v-button");
        close.addClassName("primary");
        close.getStyle().set("margin-top", "1rem");
        dialog.getFooter().add(close);

        dialog.open();
    }

    private void showManageStores() {
        getChildren()
            .filter(component -> component.getClass().equals(VerticalLayout.class))
            .forEach(this::remove);

        H2 header = new H2("🏬 Store Management");
        header.addClassName("main-header");

        Grid<StoreDTO> storeGrid = new Grid<>(StoreDTO.class, false);
        storeGrid.addColumn(store -> store.storeName).setHeader("Name");
        storeGrid.addColumn(store -> store.category).setHeader("Category");
        storeGrid.addColumn(store -> store.finalRating).setHeader("Rating");
        storeGrid.addColumn(store -> store.active ? "Active" : "Deactive").setHeader("Status");

        storeGrid.addComponentColumn(store -> {
            Button closeBtn = new Button("❌ Close Store", e -> presenter.onCloseStore(store.storeId));
            closeBtn.addClassName("v-button");
            closeBtn.addClassName("danger");
            return closeBtn;
        }).setHeader("Action");

        storeGrid.setWidthFull();
        storeGrid.addClassName("v-grid");

        List<StoreDTO> stores = presenter.getAllStores();

        if (stores.isEmpty()) {
            add(new Paragraph("❌ No stores available."));
        } else {
            storeGrid.setItems(stores);
        }

        VerticalLayout storePanel = new VerticalLayout();
        storePanel.addClassName("admin-panel-card");
        storePanel.setWidthFull();
        storePanel.add(header, storeGrid);

        add(storePanel);
    }

    private void showShutdownConfirmation() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.getElement().getClassList().add("dialog-content");
        confirmDialog.setHeaderTitle("⚠️ Confirm System Shutdown");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.getStyle().set("background-color", "#fff5f9");

        Paragraph warning = new Paragraph("⚠️ Are you sure you want to shutdown the system? This will mark the site as NOT initialized.");
        warning.getStyle().set("color", "#b91c1c");

        IntegerField keyField = new IntegerField("Enter Admin Key");
        keyField.setPlaceholder("Admin shutdown key...");
        keyField.setWidthFull();

        layout.add(warning, keyField);
        confirmDialog.add(layout);

        Button confirmBtn = new Button("🔥 Shutdown", e -> {
            Integer key = keyField.getValue();
            if (key == null) {
                NotificationView.showError("Please enter the Admin Key.");
                return;
            }
            try {
                boolean result = presenter.shutdownSystem(key);
                if (result) {
                    NotificationView.showSuccess("✅ System has been successfully shutdown.");
                    UI.getCurrent().navigate("/404");
                }
            } catch (Exception ex) {
                NotificationView.showError("Error: " + ex.getMessage());
            } finally {
                confirmDialog.close();
            }
        });

        Button cancelBtn = new Button("Cancel", e -> confirmDialog.close());
        confirmBtn.addClassName("v-button");
        confirmBtn.addClassName("danger");

        cancelBtn.addClassName("v-button");
        cancelBtn.addClassName("secondary");

        HorizontalLayout footer = new HorizontalLayout(confirmBtn, cancelBtn);
        footer.setSpacing(true);
        footer.setJustifyContentMode(JustifyContentMode.END);

        confirmDialog.getFooter().add(footer);
        confirmDialog.open();
    }
}
