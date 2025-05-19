package workshop.demo.PresentationLayer.View;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.PresentationLayer.Presenter.ManageStoreProductsPresenter;

import java.util.List;

@Route(value = "manage-products", layout = MainLayout.class)
public class ManageStoreProductsView extends VerticalLayout implements HasUrlParameter<Integer> {

    private final ManageStoreProductsPresenter presenter;
    private final VerticalLayout productList;
    private int storeId;
    private String token;

    public ManageStoreProductsView() {
        this.presenter = new ManageStoreProductsPresenter(this);
        this.productList = new VerticalLayout();

        Button addProductButton = new Button("+ Add Product", e -> openAddProductDialog());
        add(addProductButton, productList);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer storeId) {
        if (storeId == null) {
            add(new Span("❌ No store ID provided."));
            return;
        }
        this.storeId = storeId;
        this.token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            add(new Span("⚠️ You must be logged in to manage your store."));
            return;
        }
        presenter.loadProducts(storeId, token);
    }

    public void showProducts(List<ItemStoreDTO> products) {
        productList.removeAll();

        for (ItemStoreDTO item : products) {
            VerticalLayout card = new VerticalLayout();
            HorizontalLayout header = new HorizontalLayout(
                    new Span("🛒 " + item.getProductName()),
                    createEditButton(item),
                    createDeleteButton(item),
                    // createSpecialButton(item)
            );

            card.add(
                header,
                new Span("📦 Quantity: " + item.getQuantity()),
                new Span("💲 Price: " + item.getPrice()),
                new Span("📄 Description: " + item.getDescription())
            );
            card.getStyle().set("border", "1px solid #ccc").set("border-radius", "10px").set("padding", "10px");
            productList.add(card);
        }
    }

    private Button createEditButton(ItemStoreDTO item) {
        return new Button("✏️", e -> openEditDialog(item));
    }

    private Button createDeleteButton(ItemStoreDTO item) {
        return new Button("🗑️", e -> presenter.deleteProduct(storeId, token, item.getId()));
    }

    // private Button createSpecialButton(ItemStoreDTO item) {
    //     return new Button("🛍️", e -> openSpecialDialog(item));
    // }

    private void openAddProductDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Product to Store");

        TextField productIdField = new TextField("Product ID");
        TextField quantityField = new TextField("Quantity");
        TextField priceField = new TextField("Price");
        TextField categoryField = new TextField("Category");
        TextField descriptionField = new TextField("Description");

        Button submit = new Button("Add", e -> {
            presenter.handleAddProductFlow(
                storeId, token,
                productIdField.getValue(),
                quantityField.getValue(),
                priceField.getValue(),
                categoryField.getValue(),
                descriptionField.getValue(),
                dialog
            );
        });
        dialog.add(productIdField, quantityField, priceField, categoryField, descriptionField, submit);
        dialog.open();
    }

    private void openEditDialog(ItemStoreDTO item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Product: " + item.getProductName());

        TextField quantityField = new TextField("New Quantity");
        TextField priceField = new TextField("New Price");
        TextField descriptionField = new TextField("New Description");

        Button save = new Button("Save", e -> {
            presenter.updateProduct(storeId, token, item.getId(),
                quantityField.getValue(), priceField.getValue(), descriptionField.getValue());
            dialog.close();
        });
        dialog.add(quantityField, priceField, descriptionField, save);
        dialog.open();
    }

    // private void openSpecialDialog(ItemStoreDTO item) {
    //     Dialog dialog = new Dialog();
    //     dialog.setHeaderTitle("Add to Special Purchase: " + item.getProductName());

    //     TextField typeField = new TextField("Type (Auction / Lottery / Bid)");
    //     TextField param1 = new TextField("Param 1");
    //     TextField param2 = new TextField("Param 2 (optional)");

    //     Button add = new Button("Add", e -> {
    //         presenter.addToSpecialPurchase(token, storeId, item.getId(),
    //             typeField.getValue(), param1.getValue(), param2.getValue());
    //         dialog.close();
    //     });
    //     dialog.add(typeField, param1, param2, add);
    //     dialog.open();
    // }

    public void showSuccess(String msg) {
        Notification.show("✅ " + msg, 3000, Notification.Position.BOTTOM_CENTER);
        presenter.loadProducts(storeId, token);
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg, 3000, Notification.Position.BOTTOM_CENTER);
    }

    public void reopenAddProductDialogWithValues(String productId, String quantity, String price, String category, String description) {
        // reuse dialog or recreate with values
        openAddProductDialog(); // simplified for now, can be enhanced with parameters
    }
} 
