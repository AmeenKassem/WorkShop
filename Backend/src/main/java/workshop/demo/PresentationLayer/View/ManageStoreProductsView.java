package workshop.demo.PresentationLayer.View;

import java.util.ArrayList;
import java.util.Map;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.Presenter.ManageStoreDiscountsPresenter;
import workshop.demo.PresentationLayer.Presenter.ManageStoreProductsPresenter;

@Route(value = "manage-store-products", layout = MainLayout.class)
// @CssImport("./Theme/manage-products.css")
public class ManageStoreProductsView extends VerticalLayout implements HasUrlParameter<Integer> {

    private final ManageStoreProductsPresenter presenter;
    private final VerticalLayout productList = new VerticalLayout();
    private final Span errorMessage = new Span();
    private String token;
    private int storeId;
    private final ManageStoreDiscountsPresenter discPresenter = new ManageStoreDiscountsPresenter();

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

        Button addProductBtn = new Button("+ Add Item", e -> openAddItemDialog());
        Button manageDiscBtn = new Button("⚙️ Manage Discounts", e -> openDiscountDialog());

        HorizontalLayout footer = new HorizontalLayout(addProductBtn, manageDiscBtn);
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

    private void openAddItemDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Item to Your Store");

        ComboBox<ProductDTO> productSelect = new ComboBox<>("Select a Product");
        productSelect.setItemLabelGenerator(ProductDTO::getName);

        TextField priceField = new TextField("Price");
        TextField quantityField = new TextField("Quantity");

        Button addBtn = new Button(" Add", e -> {
            ProductDTO selected = productSelect.getValue();
            if (selected == null || priceField.isEmpty() || quantityField.isEmpty()) {
                Notification.show("Please fill in all fields.");
                return;
            }

            presenter.addExistingProductAsItem(
                    storeId,
                    token,
                    selected,
                    priceField.getValue(),
                    quantityField.getValue(),
                    dialog);
        });

        Button newProductBtn = new Button("Add New Product", e -> {
            dialog.close();
            openAddNewProductDialog();
        });

        VerticalLayout layout = new VerticalLayout(
                productSelect,
                priceField,
                quantityField,
                new HorizontalLayout(addBtn, newProductBtn));

        dialog.add(layout);
        dialog.open();

        presenter.loadAllProducts(token, productSelect, storeId);
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
                    new Span("🛒 " + item.getProductName()),
                    new Span("📦 Quantity: " + item.getQuantity()),
                    new Span("💲 Price: " + item.getPrice()),
                    new Span("📄 Description: " + product.getDescription()),
                    new Span("🏷️ Category: " + product.getCategory().name()));
            card.addClassName("product-card");

            Button edit = new Button("✏️ Edit", e -> openEditDialog(item, product.getDescription()));
            Button delete = new Button("🗑️ Delete", e -> presenter.deleteProduct(storeId, token, item.getProductId()));
            Button auctionButton = new Button("🎯 Start Auction",
                    e -> showAuctionDialog(storeId, token, item.getProductId()));
            Button bidButton = new Button("💸 Enable Bidding", e -> showBidDialog(storeId, token, item.getProductId()));
            Button randomButton = new Button("🎲 Start Random Draw",
                    e -> showRandomDialog(storeId, token, item.getProductId()));
            VerticalLayout actions = new VerticalLayout(edit, auctionButton, bidButton, randomButton, delete);
            actions.addClassName("button-row");

            card.add(actions);
            productList.add(card);
        }
    }

    private void openEditDialog(ItemStoreDTO item, String description) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Product");

        TextField quantityField = new TextField("Quantity");
        quantityField.setValue(String.valueOf(item.getQuantity()));

        TextField priceField = new TextField("Price");
        priceField.setValue(String.valueOf(item.getPrice()));

        Button save = new Button("Save", e -> {
            presenter.updateProduct(storeId, token, item.getProductId(),
                    quantityField.getValue(),
                    priceField.getValue(),
                    description);
            dialog.close();
        });

        dialog.add(quantityField, priceField, save);
        dialog.open();
    }

    private void openAddNewProductDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Product to Store");

        // Fields
        TextField name = new TextField("Product Name");
        TextField description = new TextField("Description");
        ComboBox<Category> category = new ComboBox<>("Category");
        category.setItems(Category.values());
        TextField price = new TextField("Price");
        TextField quantity = new TextField("Quantity");

        // Add button
        Button add = new Button("Add to Store", e -> {
            if (name.isEmpty() || description.isEmpty() || category.isEmpty()
                    || price.isEmpty() || quantity.isEmpty()) {
                Notification.show("Please fill in all fields");
                return;
            }

            presenter.addProductToStore(
                    storeId,
                    token,
                    name.getValue(),
                    description.getValue(),
                    category.getValue(),
                    price.getValue(),
                    quantity.getValue(),
                    dialog);
        });

        VerticalLayout layout = new VerticalLayout(name, description, category, price, quantity, add);
        dialog.add(layout);
        dialog.open();
    }

    public void showEmptyPage(String msg) {
        productList.removeAll();
        errorMessage.setText(msg);
        errorMessage.setVisible(true);
    }

    private void showAuctionDialog(int storeId, String token, int productId) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("🎯 Set Product to Auction");

        NumberField quantityField = new NumberField("Quantity");
        NumberField startPriceField = new NumberField("Start Price");
        NumberField timeField = new NumberField("Duration (minutes)");

        quantityField.setValue(1.0);
        startPriceField.setValue(10.0);
        timeField.setValue(60.0);

        quantityField.setMin(1);
        startPriceField.setMin(0.1);
        timeField.setMin(1);
        VerticalLayout form = new VerticalLayout(quantityField, startPriceField, timeField);
        dialog.add(form);

        Button confirm = new Button("Set Auction", event -> {
            int quantity = quantityField.getValue().intValue();
            double startPrice = startPriceField.getValue();
            long timeInMinutes = timeField.getValue().longValue();
            long timeInMillis = timeInMinutes * 60 * 1000;

            presenter.setProductToAuction(storeId, token, productId, quantity, timeInMillis, startPrice);
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(confirm, cancel);
        dialog.getFooter().add(buttons);

        dialog.open();
    }

    private void showBidDialog(int storeId, String token, int productId) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("💰 Set Product to Bid");

        NumberField quantityField = new NumberField("Quantity");
        quantityField.setValue(1.0);
        quantityField.setMin(1.0);

        VerticalLayout form = new VerticalLayout(quantityField);
        dialog.add(form);

        Button confirm = new Button("Set Bid", event -> {
            int quantity = quantityField.getValue().intValue();
            presenter.setProductToBid(storeId, token, productId, quantity);
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(new HorizontalLayout(confirm, cancel));
        dialog.open();
    }

    private void showRandomDialog(int storeId, String token, int productId) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("🎲 Set Product to Random Draw");

        NumberField quantityField = new NumberField("Quantity");
        NumberField priceField = new NumberField("Price per Ticket");
        NumberField timeField = new NumberField("Duration (minutes)");

        quantityField.setValue(1.0);
        quantityField.setMin(1.0);
        priceField.setValue(5.0);
        priceField.setMin(0.1);
        timeField.setValue(60.0);
        timeField.setMin(1.0);

        VerticalLayout form = new VerticalLayout(quantityField, priceField, timeField);
        dialog.add(form);

        Button confirm = new Button("Set Random Draw", event -> {
            int quantity = quantityField.getValue().intValue();
            double price = priceField.getValue();
            long timeInMillis = timeField.getValue().longValue() * 60 * 1000;

            presenter.setProductToRandom(storeId, token, productId, quantity, price, timeInMillis);
            dialog.close();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(new HorizontalLayout(confirm, cancel));
        dialog.open();
    }

    private void openDiscountDialog() {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Add / Combine Discounts");

        // form fields
        TextField nameField = new TextField("Name");
        NumberField percent = new NumberField("Percent (0-100)");
        percent.setMin(0);
        percent.setMax(100);
        percent.setValue(0.0);
        // TextField condition = new TextField("Condition");
        ComboBox<String> predBox = new ComboBox<>("Predicate");
        predBox.setItems("TOTAL", "QUANTITY", "CATEGORY", "PRODUCT");
        predBox.setValue("TOTAL");

        ComboBox<String> opBox = new ComboBox<>("Op");
        opBox.setItems(">", "<", ">=", "==", ":");
        opBox.setValue(">");

        TextField valueField = new TextField("Value");
        predBox.addValueChangeListener(e -> valueField.setPlaceholder(
                switch (e.getValue()) {
                    case "TOTAL" -> "₪ (number)";
                    case "QUANTITY" -> "amount";
                    case "CATEGORY" -> "name";
                    case "PRODUCT" -> "id";
                    default -> "";
                }));

        ComboBox<String> typeBox = new ComboBox<>("Type", "VISIBLE", "INVISIBLE");
        typeBox.setValue("VISIBLE");

        ComboBox<String> logicBox = new ComboBox<>("Logic",
                "SINGLE", "AND", "OR", "XOR", "MAX", "MULTIPLY");
        logicBox.setValue("SINGLE");

        CheckboxGroup<String> subs = new CheckboxGroup<>();
        subs.setLabel("Sub-discounts");
        try {
            subs.setItems(discPresenter.fetchDiscountNames(storeId, token));
        } catch (Exception ex) {
            ExceptionHandlers.handleException(ex);
        }

        Button save = new Button("Save", e -> {
            try {
                if (!valueField.isEmpty() && valueField.getValue().contains(" ")) {
                    Notification.show("Value must not contain spaces");
                    return;
                }

                discPresenter.addDiscount(
                        storeId, token,
                        nameField.getValue(),
                        percent.getValue(),
                        typeBox.getValue(),
                        (valueField.isEmpty() ? "" : predBox.getValue() + opBox.getValue() + valueField.getValue()),

                        logicBox.getValue(),
                        new ArrayList<>(subs.getSelectedItems()));
                NotificationView.showSuccess("Discount added!");
                dlg.close();
            } catch (Exception ex) {
                ExceptionHandlers.handleException(ex);
            }
        });
        Button cancel = new Button("Cancel", e -> dlg.close());

        dlg.add(new VerticalLayout(
                nameField, percent,
                new HorizontalLayout(predBox, opBox, valueField),

                new HorizontalLayout(typeBox, logicBox),
                subs,
                new HorizontalLayout(save, cancel)));
        dlg.open();
    }

}
