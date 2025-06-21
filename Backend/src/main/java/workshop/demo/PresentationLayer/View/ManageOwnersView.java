package workshop.demo.PresentationLayer.View;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
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
@CssImport("./Theme/manage-owners.css")
public class ManageOwnersView extends VerticalLayout implements HasUrlParameter<Integer> {

    private ManageOwnersPresenter presenter;
    private int storeId;
    private VerticalLayout ownersListLayout = new VerticalLayout();

    public ManageOwnersView() {
        addClassName("manage-owners-view");
        this.presenter = new ManageOwnersPresenter(this);
        ownersListLayout = new VerticalLayout();
        ownersListLayout.setSpacing(true);
        ownersListLayout.setPadding(false);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer storeId) {
        if (storeId == null) {
            add(new Span("❌ No store ID provided."));
            return;
        }
        this.storeId = storeId;

        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            add(new Span("⚠️ You must be logged in to manage owners."));
            return;
        }

        presenter.loadOwners(storeId);
    }

    public void buildManageUI(List<WorkerDTO> owners) {
        removeAll();
        add(new H2("👑 Manage Your Owners In This Store"));

        Button offerOwnerButton = new Button("➕ Offer to Add Owner", e -> showOfferOwnerDialog());
        offerOwnerButton.addClassName("offer-button");

        add(offerOwnerButton);

        ownersListLayout.removeAll();

        if (owners == null || owners.isEmpty()) {
            ownersListLayout.add(new Paragraph("No owners assigned yet."));
        } else {
            for (WorkerDTO owner : owners) {
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();
                row.addClassName("owner-row");
                Span ownerInfo = new Span("👤 " + owner.getUsername());
                Button deleteBtn = new Button("Delete", e -> showDeleteDialog(owner));
                deleteBtn.addClassName("delete-button");

                row.add(ownerInfo, deleteBtn);
                row.setJustifyContentMode(JustifyContentMode.BETWEEN);

                ownersListLayout.add(row);
            }
        }

        add(ownersListLayout);
    }

    private void showDeleteDialog(WorkerDTO owner) {
        Dialog dialog = new Dialog();
        dialog.add(new Paragraph("Are you sure you want to delete owner: " + owner.getUsername() + "?"));
        System.out.println("id to delete");
        System.out.println(owner.getWorkerId());
        Button confirm = new Button("Delete", e -> {
            presenter.deleteOwner(storeId, owner.getWorkerId());

            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout(confirm, cancel);
        dialog.add(actions);
        dialog.open();
    }

    private void showOfferOwnerDialog() {
        Dialog dialog = new Dialog();
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.addClassName("offer-dialog");

        TextField usernameField = new TextField("New owner's username");
        usernameField.setPlaceholder("e.g., johndoe");

        Button confirm = new Button("Send Offer", e -> {
            String newOwnerUsername = usernameField.getValue();
            if (newOwnerUsername != null && !newOwnerUsername.isBlank()) {
                presenter.offerToAddOwner(storeId, newOwnerUsername);
                dialog.close();
            } else {
                NotificationView.showError("Username cannot be empty.");
            }
        });

        Button cancel = new Button("Cancel", e -> dialog.close());

        layout.add(usernameField, new HorizontalLayout(confirm, cancel));
        dialog.add(layout);
        dialog.open();
    }
}
