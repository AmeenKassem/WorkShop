package workshop.demo.PresentationLayer.View;

import java.util.Map;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.PresentationLayer.Presenter.ManageStoreProductsPresenter;

@Route(value = "manage-store-products", layout = MainLayout.class)
// @CssImport("./Theme/manage-products.css")
public class ManageStoreProductsView extends VerticalLayout implements HasUrlParameter<Integer> {

    private final ManageStoreProductsPresenter presenter;
    private final VerticalLayout productList = new VerticalLayout();
    private final Span errorMessage = new Span();
    private String token;
    private int storeId;

    public ManageStoreProductsView() {
        this.presenter = new ManageStoreProductsPresenter(this);
        addClassName("manage-products-container");

        Span title = new Span("Manage Products Page:");
        title.addClassName("page-title");

        errorMessage.addClassName("error-message");
        errorMessage.setVisible(false);

        productList.addClassName("product-list");
        Div productSection = new Div(productList);
        productSection.addClassName("products-section");

        Button addProductBtn = new Button("+ Add Product", e -> openAddProductDialog());
        addProductBtn.addClassName("add-product-btn");

        Button backBtn = new Button("⬅ Back", e -> getUI().ifPresent(ui -> ui.navigate("my stores")));
        backBtn.addClassName("back-btn");

        HorizontalLayout footer = new HorizontalLayout(backBtn, addProductBtn);
        footer.addClassName("footer-buttons");
        footer.setWidthFull();

        add(title, errorMessage, productSection, footer);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer storeId) {
        this.token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        this.storeId = storeId;
        if (token == null) {
            Notification.show("⚠️ You must be logged in.");
            return;
        }
        presenter.loadProducts(storeId, token);
    }

    public void showProducts(Map<ItemStoreDTO, ProductDTO> products) {
        productList.removeAll();
        errorMessage.setVisible(false);

        if (products == null || products.isEmpty()) {
            showEmptyPage("📭 No products in this store yet.");
            return;
        }

        for (Map.Entry<ItemStoreDTO, ProductDTO> entry : products.entrySet()) {
            ItemStoreDTO item = entry.getKey();
            ProductDTO product = entry.getValue();

            VerticalLayout card = new VerticalLayout(
                    new Span("🛒 " + product.getName()),
                    new Span("📦 Quantity: " + item.getQuantity()),
                    new Span("💲 Price: " + item.getPrice()));
            card.addClassName("product-card");

            Button edit = new Button("✏️ Edit", e -> openEditDialog(item, product.getDescription()));
            Button delete = new Button("🗑️ Delete", e -> presenter.deleteProduct(storeId, token, item.getId()));

            HorizontalLayout actions = new HorizontalLayout(edit, delete);
            actions.addClassName("button-row");

            card.add(actions);
            productList.add(card);
        }
    }

    private void openAddProductDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Product");

        TextField name = new TextField("Product Name");
        TextField description = new TextField("Description");
        ComboBox<Category> category = new ComboBox<>("Category");
        category.setItems(Category.values());
        TextField keywords = new TextField("Keywords (comma-separated)");
        TextField price = new TextField("Price");
        TextField quantity = new TextField("Quantity");

        Button add = new Button("Add", e -> presenter.addProductToStore(storeId, token,
                name.getValue(),
                description.getValue(),
                category.getValue(),
                keywords.getValue(),
                price.getValue(),
                quantity.getValue(),
                dialog));

        dialog.add(name, description, category, keywords, price, quantity, add);
        dialog.open();
    }

    private void openEditDialog(ItemStoreDTO item, String description) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Product");

        TextField quantityField = new TextField("Quantity");
        quantityField.setValue(String.valueOf(item.getQuantity()));

        TextField priceField = new TextField("Price");
        priceField.setValue(String.valueOf(item.getPrice()));

        Button save = new Button("Save", e -> {
            presenter.updateProduct(storeId, token, item.getId(),
                    quantityField.getValue(),
                    priceField.getValue(),
                    description);
            dialog.close();
        });

        dialog.add(quantityField, priceField, save);
        dialog.open();
    }

    public void showSuccess(String msg) {
        Notification.show("✅ " + msg);
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg);
        errorMessage.setText(msg);
        errorMessage.setVisible(true);
    }

    public void showEmptyPage(String msg) {
        productList.removeAll();
        errorMessage.setText(msg);
        errorMessage.setVisible(true);
    }
}
