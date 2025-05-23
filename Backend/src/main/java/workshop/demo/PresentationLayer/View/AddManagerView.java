package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.PresentationLayer.Presenter.AddManagerPresenter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "add-manager", layout = MainLayout.class)
@CssImport("./Theme/addManagerTheme.css")
public class AddManagerView extends VerticalLayout implements HasUrlParameter<Integer> {

    private final TextField fullNameField = new TextField("Full name");
    private final Map<Permission, Checkbox> checkboxMap = new HashMap<>();
    private final Button saveButton = new Button("Save changes");
    private final Button backButton = new Button("Back to store page");

    private final AddManagerPresenter presenter;

    private int myStoreId = -1;

    public AddManagerView() {
        addClassName("add-manager-view");
        presenter = new AddManagerPresenter(this);

        H1 title = new H1("Add New Owner Or Manager");
        title.addClassName("add-manager-title");

        fullNameField.addClassName("add-manager-field");

        Label permissionLabel = new Label("Select Permissions");
        permissionLabel.addClassName("add-manager-subtitle");

        VerticalLayout permissionsLayout = new VerticalLayout();
        permissionsLayout.addClassName("add-manager-permissions");

        for (Permission p : Permission.values()) {
            Checkbox cb = new Checkbox(getPermissionLabel(p));
            checkboxMap.put(p, cb);
            permissionsLayout.add(cb);
        }

        saveButton.addClassName("add-manager-button");
        backButton.addClassName("add-manager-button");

        saveButton.addClickListener(e -> presenter.sendAddManagerRequest(getFullName(), getSelectedPermissions()));
        backButton.addClickListener(e -> presenter.navigateBack(String.valueOf(myStoreId)));

        HorizontalLayout buttonLayout = new HorizontalLayout(backButton, saveButton);
        buttonLayout.addClassName("add-manager-button-layout");

        VerticalLayout formLayout = new VerticalLayout(title, fullNameField, permissionLabel, permissionsLayout,
                buttonLayout);
        formLayout.addClassName("add-manager-form");

        add(formLayout);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer storeId) {
        if (storeId == null) {
            add(new Span("❌ No store ID provided."));
            return;
        }

        System.out.println("🚀 setParameter called with storeId = " + storeId);

        this.myStoreId = storeId;
        presenter.setStoreId(storeId);

        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            add(new Span("⚠️ You must be logged in to add a manager."));
            return;
        }

    }

    public String getFullName() {
        return fullNameField.getValue();
    }

    public Set<Permission> getSelectedPermissions() {
        return checkboxMap.entrySet().stream()
                .filter(entry -> entry.getValue().getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public void showSuccess(String msg) {
        Notification.show("✅ " + msg, 3000, Notification.Position.BOTTOM_CENTER);
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg, 5000, Notification.Position.BOTTOM_CENTER);
    }

    private String getPermissionLabel(Permission p) {
        return switch (p) {
            case ViewAllProducts ->
                "View all products";
            case AddToStock ->
                "Add new Product to the store";
            case DeleteFromStock ->
                "Delete existing product from the store";
            case UpdateQuantity ->
                "Handle customer complaints";
            case UpdatePrice ->
                "Change the price of products";
            case SpecialType ->
                "Manage other managers";
            case MANAGE_STORE_POLICY ->
                throw new UnsupportedOperationException("Unimplemented case: " + p);
            default ->
                throw new IllegalArgumentException("Unexpected value: " + p);
        };
    }

}
