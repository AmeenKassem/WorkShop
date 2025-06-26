package workshop.demo.PresentationLayer.View;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.PresentationLayer.Presenter.ManageStoreManagersPresenter;

@Route(value = "manage-store-managers", layout = MainLayout.class)
 @CssImport("./Theme/manageStoreTheme.css")
public class ManageStoreManagersView extends VerticalLayout implements HasUrlParameter<Integer> {

    private final ManageStoreManagersPresenter presenter;
    private final VerticalLayout managersListLayout = new VerticalLayout();
    private final Dialog addManagerDialog = new Dialog();
    private final TextField usernameField = new TextField("Username");
    private final Map<Permission, Checkbox> addDialogPermissionCheckboxes = new HashMap<>();
    private int myStoreId = -1;

    public ManageStoreManagersView() {
        this.presenter = new ManageStoreManagersPresenter(this);

        addClassName("add-manager-view");
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.START);
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("Manage Store Managers");
        title.addClassName("title");

        Button addManagerBtn = new Button("Add Manager", event -> openAddManagerDialog());
        addManagerBtn.addClassName("add-manager-button");

        managersListLayout.addClassName("manager-form-container");

        configureAddManagerDialog();

        add(title, addManagerBtn, managersListLayout);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer storeId) {
        if (storeId == null) {
            showError("❌ No store ID provided.");
            return;
        }

        this.myStoreId = storeId;
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            showError("⚠️ You must be logged in to manage your store.");
            return;
        }

        presenter.setStoreId(String.valueOf(storeId));
    }

    private void configureAddManagerDialog() {
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.addClassName("manager-form-container");

        VerticalLayout inputSection = new VerticalLayout();
        inputSection.addClassName("input-section");
        usernameField.setWidthFull();
        inputSection.add(usernameField);

        VerticalLayout permissionsLayout = new VerticalLayout();
        permissionsLayout.addClassName("permission-section");

        for (Permission permission : Permission.values()) {
            Checkbox checkbox = new Checkbox(permission.name());
            checkbox.addClassName("permission-checkbox");
            addDialogPermissionCheckboxes.put(permission, checkbox);
            permissionsLayout.add(checkbox);
        }

        HorizontalLayout buttonRow = new HorizontalLayout();
        buttonRow.addClassName("button-row");

        Button sendBtn = new Button("Send", event -> {
            try {
                presenter.addManager(usernameField.getValue(), addDialogPermissionCheckboxes);
                addManagerDialog.close();
            } catch (Exception e) {
                showError("Failed to add manager: " + e.getMessage());
            }
        });
        sendBtn.addClassName("add-manager-button");

        buttonRow.add(sendBtn);

        dialogLayout.add(inputSection, permissionsLayout, buttonRow);
        addManagerDialog.add(dialogLayout);
    }

    private void openAddManagerDialog() {
        usernameField.clear();
        addDialogPermissionCheckboxes.values().forEach(cb -> cb.setValue(false));
        addManagerDialog.open();
    }

    public void updateManagerList(List<WorkerDTO> managers) {
        managersListLayout.removeAll();

        boolean hasVisibleManagers = false;

        for (WorkerDTO manager : managers) {
            if (!manager.isManager() && !manager.isSetByMe()) {
                continue;
            }

            hasVisibleManagers = true;

            VerticalLayout managerBlock = new VerticalLayout();
            managerBlock.addClassName("manager-card");

            Paragraph name = new Paragraph(manager.getUsername());
            name.addClassName("manager-name");

            Map<Permission, Checkbox> checkboxMap = new HashMap<>();
            VerticalLayout permissions = new VerticalLayout();
            permissions.setSpacing(false);
            permissions.setPadding(false);

            for (Permission permission : Permission.values()) {
                Checkbox checkbox = new Checkbox(permission.name());
                checkbox.setValue(Arrays.asList(manager.getPermessions()).contains(permission));
                checkboxMap.put(permission, checkbox);
                permissions.add(checkbox);
            }

            Button saveBtn = new Button("Save", e -> presenter.savePermissions(manager.getWorkerId(), checkboxMap));
            saveBtn.addClassName("save-permissions-button");

            Button deleteBtn = new Button("Delete", e -> presenter.deleteManager(manager.getWorkerId()));
            deleteBtn.addClassName("delete-manager-button");

            HorizontalLayout actionRow = new HorizontalLayout(saveBtn, deleteBtn);
            actionRow.addClassName("button-row");

            managerBlock.add(name, permissions, actionRow);
            managersListLayout.add(managerBlock);
        }

        if (!hasVisibleManagers) {
            Paragraph emptyMsg = new Paragraph("No managers assigned to this store yet.");
            emptyMsg.addClassName("empty-managers-message");
            managersListLayout.add(emptyMsg);
        }
    }


    public void showSuccess(String message) {
        NotificationView.showSuccess(message);
    }

    public void showError(String message) {
        NotificationView.showError(message);
    }
}
