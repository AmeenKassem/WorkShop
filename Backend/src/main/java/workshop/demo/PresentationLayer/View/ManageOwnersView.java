package workshop.demo.PresentationLayer.View;

import java.util.List;

import com.vaadin.flow.component.button.Button;
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
import workshop.demo.PresentationLayer.Presenter.ManageOwnersPresenter;

@Route(value = "manageMyOwners", layout = MainLayout.class)
@CssImport("./Theme/manageStoreTheme.css")
public class ManageOwnersView extends VerticalLayout implements HasUrlParameter<Integer> {

    private ManageOwnersPresenter presenter;
    private int storeId;
    private VerticalLayout ownersListLayout = new VerticalLayout();

    public ManageOwnersView() {
        addClassName("add-manager-view");
        this.presenter = new ManageOwnersPresenter(this);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.START);
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("👑 Manage Store Owners");
        title.addClassName("title");

        Button offerOwnerButton = new Button("Add Owner", e -> showOfferOwnerDialog());
        offerOwnerButton.addClassName("add-manager-button");

        ownersListLayout.addClassName("manager-form-container");

        add(title, offerOwnerButton, ownersListLayout);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer storeId) {
        if (storeId == null) {
            NotificationView.showError("❌ No store ID provided.");
            return;
        }
        this.storeId = storeId;

        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            NotificationView.showError("⚠️ You must be logged in to manage owners.");
            return;
        }

        presenter.loadOwners(storeId);
    }

    public void buildManageUI(List<WorkerDTO> owners) {
        ownersListLayout.removeAll();

        if (owners == null || owners.isEmpty()) {
            Paragraph emptyMsg = new Paragraph("No owners assigned to this store yet.");
            emptyMsg.addClassName("empty-managers-message");
            ownersListLayout.add(emptyMsg);
        } else {
            for (WorkerDTO owner : owners) {
                VerticalLayout card = new VerticalLayout();
                card.addClassName("manager-card");

                Paragraph name = new Paragraph(owner.getUsername());
                name.addClassName("manager-name");

                Button deleteBtn = new Button("Delete", e -> showDeleteDialog(owner));
                deleteBtn.addClassName("delete-manager-button");

                card.add(name, deleteBtn);
                ownersListLayout.add(card);
            }
        }
    }

    private void showDeleteDialog(WorkerDTO owner) {
        Dialog dialog = new Dialog();
        dialog.add(new Paragraph("Are you sure you want to delete owner: " + owner.getUsername() + "?"));

        Button confirm = new Button("Delete", e -> {
            presenter.deleteOwner(storeId, owner.getWorkerId());
            dialog.close();
        });
        confirm.addClassName("delete-manager-button");

        Button cancel = new Button("Cancel", e -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout(confirm, cancel);
        actions.addClassName("button-row");
        dialog.add(actions);
        dialog.open();
    }

    private void showOfferOwnerDialog() {
        Dialog dialog = new Dialog();
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("manager-form-container");

        VerticalLayout inputSection = new VerticalLayout();
        inputSection.addClassName("input-section");

        TextField usernameField = new TextField("New owner's username");
        usernameField.setPlaceholder("e.g., johndoe");
        usernameField.setWidthFull();
        inputSection.add(usernameField);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addClassName("button-row");

        Button confirm = new Button("Send", e -> {
            String newOwnerUsername = usernameField.getValue();
            if (newOwnerUsername != null && !newOwnerUsername.isBlank()) {
                presenter.offerToAddOwner(storeId, newOwnerUsername);
                dialog.close();
            } else {
                NotificationView.showError("Username cannot be empty.");
            }
        });
        confirm.addClassName("add-manager-button");

        Button cancel = new Button("Cancel", e -> dialog.close());

        buttons.add(confirm, cancel);
        layout.add(inputSection, buttons);
        dialog.add(layout);
        dialog.open();
    }
}
