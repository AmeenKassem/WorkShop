package workshop.demo.PresentationLayer.View;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
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
public class AdminPageView extends VerticalLayout {

    private final AdminPagePresenter presenter;

    public AdminPageView() {
        presenter = new AdminPagePresenter(this);
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        add(new H2("Admin Control Panel"));

        Button viewHistoryBtn = new Button("📊 View Purchase History", e -> UI.getCurrent().navigate("admin-purchase-history"));

        Button manageUsersBtn = new Button("👥 Manage Users", e -> showManageUsers());
        Button manageStoresBtn = new Button("👥 Manage Stores", e -> showManageStores());
        Button shutdownSystemBtn = new Button("❌ Shutdown System", e -> showShutdownConfirmation());

        VerticalLayout actionButtons = new VerticalLayout(viewHistoryBtn, manageUsersBtn, manageStoresBtn, shutdownSystemBtn);
        add(actionButtons);
    }

    private void showManageUsers() {
        removeAll(); // Clear current content

        add(new H2("User Suspension Management!"));

        Button viewSuspensionsBtn = new Button("👁️ View Suspensions", e -> showSuspensionsDialog());
        add(viewSuspensionsBtn);

        Grid<UserDTO> userGrid = new Grid<>(UserDTO.class, false);

        userGrid.addColumn(user -> user.username != null ? user.username : "Guest")
                .setHeader("Username");
        userGrid.addColumn(user -> user.age).setHeader("Age");
        userGrid.addColumn(user -> Boolean.TRUE.equals(user.isOnline) ? "Online" : "Offline")
                .setHeader("Status");
        userGrid.addColumn(user -> Boolean.TRUE.equals(user.isAdmin) ? "Admin" : "User")
                .setHeader("Role");

        userGrid.addComponentColumn(user -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button suspend = new Button("Suspend", click -> {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Suspend User");

                TextField minutesField = new TextField("Suspend for (minutes)");
                Button confirm = new Button("OK", e -> {
                    try {
                        int minutes = Integer.parseInt(minutesField.getValue());
                        presenter.onSuspendUser(user.id, minutes);
                        dialog.close();
                    } catch (NumberFormatException ex) {
                        NotificationView.showError("Invalid input");
                    }
                });

                dialog.add(new VerticalLayout(minutesField, confirm));
                dialog.open();
            });
            Button pause = new Button("Pause", click -> presenter.onPauseSuspension(user.getId()));
            Button resume = new Button("Resume", click -> presenter.onResumeSuspension(user.getId()));
            Button cancel = new Button("Cancel", click -> presenter.onCancelSuspension(user.getId()));
            actions.add(suspend, cancel, pause, resume);
            return actions;
        }).setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);
        setFlexGrow(0);

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

        add(userGrid);
    }

    private void showSuspensionsDialog() {
        List<UserSuspensionDTO> suspensions = presenter.onViewSuspensions();

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Suspended Users");
        dialog.setWidth("700px");
        dialog.setHeight("400px");

        if (suspensions.isEmpty()) {
            dialog.add(new Paragraph("No suspended users."));
        } else {
            Grid<UserSuspensionDTO> grid = new Grid<>(UserSuspensionDTO.class, false);

            grid.addColumn(UserSuspensionDTO::getUserName)
                    .setHeader("User Name");

            grid.addColumn(s -> s.isPaused() ? "Yes" : "No")
                    .setHeader("Paused");

            grid.addColumn(s -> s.getSuspensionEndTime() != null ? s.getSuspensionEndTime().toString() : "N/A")
                    .setHeader("End Time")
                    .setAutoWidth(true)
                    .setFlexGrow(0);

            grid.addColumn(UserSuspensionDTO::getRemainingWhenPaused)
                    .setHeader("Remaining Time ")
                    .setAutoWidth(true)
                    .setFlexGrow(0);

            grid.setItems(suspensions);
            grid.setWidthFull();
            grid.setHeight("300px");

            dialog.add(grid);
        }

        Button close = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(close);

        dialog.open();
    }

    private void showManageStores() {
        removeAll(); // Clear current content
        add(new H2("Manage Stores"));
        List<StoreDTO> stores = presenter.getAllStores();

        if (stores.isEmpty()) {
            add(new Paragraph("❌ No stores available."));
        }
        Grid<StoreDTO> storeGrid = new Grid<>(StoreDTO.class, false);

        storeGrid.addColumn(store -> store.storeName).setHeader("Name");
        storeGrid.addColumn(store -> store.category).setHeader("Category");
        storeGrid.addColumn(store -> store.finalRating).setHeader("Rating");
        storeGrid.addColumn(store -> store.active ? "Active" : "Deactive").setHeader("Status");

        storeGrid.addComponentColumn(store -> {
            Button closeBtn = new Button("❌ Close Store", e -> presenter.onCloseStore(store.storeId));
            return closeBtn;
        }).setHeader("Action");

        storeGrid.setItems(stores);
        add(storeGrid);

    }

    private void showShutdownConfirmation() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Shutdown");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        Paragraph warning = new Paragraph("Are you sure you want to shutdown the system? This will mark the site as NOT initialized.");
        IntegerField keyField = new IntegerField("Enter Admin Key");
        keyField.setPlaceholder("Enter the shutdown key...");

        layout.add(warning, keyField);
        confirmDialog.add(layout);

        Button confirmBtn = new Button("Shutdown", e -> {
            Integer key = keyField.getValue();
            if (key == null) {
                NotificationView.showError("Please enter the Admin Key.");
                return;
            }
            try {
                boolean result = presenter.shutdownSystem(key);
                if (result) {
                    NotificationView.showSuccess("System has been successfully shutdown.");
                    UI.getCurrent().navigate("/404");
                }
            } catch (Exception ex) {
                NotificationView.showError("Error: " + ex.getMessage());
            } finally {
                confirmDialog.close();
            }
        });

        Button cancelBtn = new Button("Cancel", e -> confirmDialog.close());

        confirmDialog.getFooter().add(confirmBtn, cancelBtn);
        confirmDialog.open();
    }

}
