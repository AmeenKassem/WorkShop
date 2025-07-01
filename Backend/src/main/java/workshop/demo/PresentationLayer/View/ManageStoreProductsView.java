package workshop.demo.PresentationLayer.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.CreateDiscountDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.Presenter.ManageStoreDiscountsPresenter;
import workshop.demo.PresentationLayer.Presenter.ManageStoreProductsPresenter;
import com.vaadin.flow.component.dependency.CssImport;

@Route(value = "manage-store-products", layout = MainLayout.class)
@CssImport("./Theme/manage-products.css")
public class ManageStoreProductsView extends VerticalLayout implements HasUrlParameter<Integer> {

    private final ManageStoreProductsPresenter presenter;
    private final VerticalLayout productList = new VerticalLayout();
    private final Span errorMessage = new Span();
    private String token;
    private int storeId;
    private final ManageStoreDiscountsPresenter discPresenter = new ManageStoreDiscountsPresenter();
    private Map<ItemStoreDTO, ProductDTO> currentProducts = Map.of();

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
        addProductBtn.addClassName("add-product-btn");
        manageDiscBtn.addClassName("back-btn");

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
            NotificationView.showError("⚠️ You must be logged in.");
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

        Button addBtn = new Button("Add", e -> {
            ProductDTO selected = productSelect.getValue();
            if (selected == null || priceField.isEmpty() || quantityField.isEmpty()) {
                NotificationView.showInfo("Please fill in all fields.");
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
        addBtn.addClassNames("dialog-button", "confirm");

        Button newProductBtn = new Button("Add New Product", e -> {
            dialog.close();
            openAddNewProductDialog();
        });
        newProductBtn.addClassNames("dialog-button", "cancel");

        VerticalLayout layout = new VerticalLayout(
                productSelect,
                priceField,
                quantityField);
        layout.addClassName("dialog-content");

        dialog.add(layout);
        dialog.getFooter().add(new HorizontalLayout(addBtn, newProductBtn));
        dialog.open();

        presenter.loadAllProducts(token, productSelect, storeId);
    }

    public void showProducts(Map<ItemStoreDTO, ProductDTO> products) {
        this.currentProducts = (products == null ? Map.of() : products);

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
            Button policyButton = new Button("Add Policy For Product",
                    e -> showPolicyDialog(storeId, token, item.getProductId()));
            Button remPolicyButton = new Button("❌ Remove Policy",
                    e -> showRemovePolicyDialog(storeId, token, item.getProductId()));
            HorizontalLayout row1 = new HorizontalLayout(edit, auctionButton, delete);
            HorizontalLayout row2 = new HorizontalLayout(bidButton, randomButton);
            HorizontalLayout row3 = new HorizontalLayout(policyButton);
            row1.addClassName("button-row");
            row2.addClassName("button-row");

            VerticalLayout actions = new VerticalLayout(row1, row2, row3);
            actions.setSpacing(true);
            actions.setJustifyContentMode(JustifyContentMode.END);

            card.add(actions);
            productList.add(card);
        }
    }

    private void showRemovePolicyDialog(int storeId, String token, int productId) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        Paragraph title = new Paragraph("Select the purchase policy/policies to remove:");

        Checkbox agePolicyCheckbox = new Checkbox("🧓 Cannot buy under age");
        Checkbox quantityPolicyCheckbox = new Checkbox("📦 Cannot buy less than quantity");

        Button removeBtn = new Button("Remove", event -> {
            boolean selected = false;

            if (agePolicyCheckbox.getValue()) {
                presenter.removeAgeRestrictionPolicy(storeId, token, productId, null);
                selected = true;
            }

            if (quantityPolicyCheckbox.getValue()) {
                presenter.removeMinQuantityPolicy(storeId, token, productId, null);
                selected = true;
            }

            if (!selected) {
                Notification.show("Please select at least one policy to remove.", 3000, Notification.Position.MIDDLE);
                return;
            }

            dialog.close();
            Notification.show("Selected policies removed.", 3000, Notification.Position.TOP_CENTER);
        });

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(removeBtn, cancelBtn);

        layout.add(title, agePolicyCheckbox, quantityPolicyCheckbox, buttons);
        dialog.add(layout);
        dialog.open();
    }

    private void showPolicyDialog(int storeId, String token, int productId) {
        Dialog policyDialog = new Dialog();
        policyDialog.setCloseOnOutsideClick(true);
        policyDialog.setCloseOnEsc(true);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);
        dialogLayout.setWidth("400px");

        // Policy 1: Age Restriction
        Checkbox ageCheckbox = new Checkbox("Cannot buy under age:");
        TextField ageInput = new TextField();
        ageInput.setPlaceholder("Enter minimum age");
        ageInput.setVisible(false);

        ageCheckbox.addValueChangeListener(event -> {
            ageInput.setVisible(event.getValue());
        });

        // Policy 2: Minimum Quantity Restriction
        Checkbox quantityCheckbox = new Checkbox("Cannot buy less than quantity:");
        TextField quantityInput = new TextField();
        quantityInput.setPlaceholder("Enter minimum quantity");
        quantityInput.setVisible(false);

        quantityCheckbox.addValueChangeListener(event -> {
            quantityInput.setVisible(event.getValue());
        });

        // Buttons
        Button submit = new Button("Submit", e -> {
            if (ageCheckbox.getValue()) {
                try {
                    int minAge = Integer.parseInt(ageInput.getValue());
                    presenter.addAgeRestrictionPolicy(storeId, token, productId, minAge);
                } catch (NumberFormatException ex) {
                    Notification.show("Invalid age input", 3000, Notification.Position.MIDDLE);
                    return;
                }
            }

            if (quantityCheckbox.getValue()) {
                try {
                    int minQty = Integer.parseInt(quantityInput.getValue());
                    presenter.addMinQuantityPolicy(storeId, token, productId, minQty);
                } catch (NumberFormatException ex) {
                    Notification.show("Invalid quantity input", 3000, Notification.Position.MIDDLE);
                    return;
                }
            }

            policyDialog.close();
            Notification.show("Policies submitted", 3000, Notification.Position.TOP_CENTER);
        });

        Button cancel = new Button("Cancel", e -> policyDialog.close());
        HorizontalLayout buttons = new HorizontalLayout(submit, cancel);

        dialogLayout.add(ageCheckbox, ageInput, quantityCheckbox, quantityInput, buttons);
        policyDialog.add(dialogLayout);
        policyDialog.open();
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
        save.addClassNames("dialog-button", "confirm");

        VerticalLayout layout = new VerticalLayout(quantityField, priceField);
        layout.addClassName("dialog-content");

        dialog.add(layout);
        dialog.getFooter().add(new HorizontalLayout(save));
        dialog.open();
    }

    private void openAddNewProductDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Product to Store");

        TextField name = new TextField("Product Name");
        TextField description = new TextField("Description");
        TextField keyword = new TextField("Keyword");
        keyword.setPlaceholder("e.g. summer, electronics, sport...");

        Label keywordHelp = new Label("Enter a keyword that best describes the product (required)");

        ComboBox<Category> category = new ComboBox<>("Category");
        category.setItems(Category.values());
        TextField price = new TextField("Price");
        TextField quantity = new TextField("Quantity");

        Button add = new Button("Add to Store", e -> {
            if (name.isEmpty() || description.isEmpty() || keyword.isEmpty() || category.isEmpty()
                    || price.isEmpty() || quantity.isEmpty()) {
                NotificationView.showInfo("Please fill in all fields");
                return;
            }

            presenter.addProductToStore(
                    storeId,
                    token,
                    name.getValue(),
                    description.getValue(),
                    keyword.getValue(),
                    category.getValue(),
                    price.getValue(),
                    quantity.getValue(),
                    dialog);
        });
        add.addClassNames("dialog-button", "confirm");

        VerticalLayout layout = new VerticalLayout(
                name, description, keywordHelp, keyword, category, price, quantity);
        layout.addClassName("dialog-content");

        dialog.add(layout);
        dialog.getFooter().add(new HorizontalLayout(add));
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
        confirm.addClassName("dialog-button");
        confirm.addClassName("confirm");

        cancel.addClassName("dialog-button");
        cancel.addClassName("cancel");
        HorizontalLayout buttons = new HorizontalLayout(confirm, cancel);
        buttons.addClassName("dialog-button-row");

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
        form.addClassName("dialog-content");
        dialog.add(form);

        Button confirm = new Button("Set Bid", event -> {
            int quantity = quantityField.getValue().intValue();
            presenter.setProductToBid(storeId, token, productId, quantity);
            dialog.close();
        });
        confirm.addClassNames("dialog-button", "confirm");

        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addClassNames("dialog-button", "cancel");

        dialog.getFooter().add(new HorizontalLayout(confirm, cancel));
        dialog.open();
    }

    private void showRandomDialog(int storeId, String token, int productId) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("🎲 Set Product to Random Draw");

        NumberField quantityField = new NumberField("Quantity");
        NumberField priceField = new NumberField("Full Price");
        NumberField timeField = new NumberField("Duration (minutes)");

        quantityField.setValue(1.0);
        quantityField.setMin(1.0);
        priceField.setValue(5.0);
        priceField.setMin(0.1);
        timeField.setValue(60.0);
        timeField.setMin(1.0);

        VerticalLayout form = new VerticalLayout(quantityField, priceField, timeField);
        form.addClassName("dialog-content");
        dialog.add(form);

        Button confirm = new Button("Set Random Draw", event -> {
            int quantity = quantityField.getValue().intValue();
            double price = priceField.getValue();
            long timeInMillis = timeField.getValue().longValue() * 60 * 1000;

            presenter.setProductToRandom(storeId, token, productId, quantity, price, timeInMillis);
            dialog.close();
        });
        confirm.addClassNames("dialog-button", "confirm");

        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addClassNames("dialog-button", "cancel");

        dialog.getFooter().add(new HorizontalLayout(confirm, cancel));
        dialog.open();
    }

    private void openDiscountDialog() {
    Dialog dlg = new Dialog();
    dlg.setHeaderTitle("Add / Combine Discounts");

    VerticalLayout container = new VerticalLayout();
    DiscountFormEditor rootEditor = new DiscountFormEditor();
    container.add(rootEditor);

    TreeGrid<CreateDiscountDTO> tree = new TreeGrid<>();
    tree.addComponentHierarchyColumn(discount -> {
        Span nameSpan = new Span(discount.getName());
        nameSpan.getElement().setProperty("title", discount.getName());
        return nameSpan;
    }).setHeader("Name")
      .setAutoWidth(true)
      .setFlexGrow(2)
      .setResizable(true)
      .setWidth("300px");

    tree.addColumn(CreateDiscountDTO::getPercent).setHeader("Percent");
    tree.addColumn(d -> (d.getCondition() == null || d.getCondition().isBlank()) ? "No condition" : d.getCondition())
        .setHeader("Condition");
    tree.addColumn(d -> d.getLogic() != null ? d.getLogic().name() : "SINGLE").setHeader("Logic");
    tree.addColumn(d -> d.getType() != null ? d.getType().name() : "VISIBLE").setHeader("Type");

    CreateDiscountDTO root = discPresenter.fetchDiscountTree(storeId, token);
    if (root != null) {
        tree.setItems(List.of(root), CreateDiscountDTO::getSubDiscounts);
    } else {
        NotificationView.showInfo("No discount tree found.");
    }

    tree.setHeight("300px");
    tree.setWidthFull();

    Button deleteAllBtn = new Button("🗑 Clear All Discounts", ev -> {
        try {
            List<String> all = discPresenter.fetchDiscountNames(storeId, token);
            if (all.isEmpty()) {
                NotificationView.showInfo("There are no discounts to delete.");
                return;
            }

            for (String name : all) {
                try {
                    discPresenter.deleteDiscount(storeId, token, name);
                } catch (Exception ex) {
                    ExceptionHandlers.handleException(ex); // Log but continue
                }
            }

            NotificationView.showSuccess("All discounts deleted.");
            tree.setItems(List.of()); // Clear tree from UI too
        } catch (Exception ex) {
            ExceptionHandlers.handleException(ex);
        }
    });

    Button save = new Button("Save", e -> {
        try {
            CreateDiscountDTO dto = rootEditor.buildDTO();
            discPresenter.addDiscount(storeId, token, dto);
            NotificationView.showSuccess("Discount added!");
            dlg.close();
        } catch (Exception ex) {
            ExceptionHandlers.handleException(ex);
        }
    });

    Button cancel = new Button("Cancel", e -> dlg.close());

    // ✅ Wrap everything
    VerticalLayout wrapper = new VerticalLayout();
    wrapper.addClassName("discount-dialog-wrapper");

    wrapper.add(
        container,
        deleteAllBtn,
        new Span("Current Discounts in Store:"),
        tree,
        new HorizontalLayout(save, cancel)
    );

    dlg.add(wrapper);
    dlg.open();
}

    private class DiscountFormEditor extends VerticalLayout {
        private final TextField name = new TextField("Name");
        private final NumberField percent = new NumberField("Percent (0–100)");
        private final ComboBox<CreateDiscountDTO.Type> type = new ComboBox<>("Type");
        private final ComboBox<CreateDiscountDTO.Logic> logic = new ComboBox<>("Logic");
        private final ComboBox<String> predicate = new ComboBox<>("Predicate");
        private final Div valueWrapper = new Div();
        private final VerticalLayout subEditors = new VerticalLayout();
        private final Button addSubBtn = new Button("+ Add Sub-Discount");

        public DiscountFormEditor() {
            name.setRequired(true);
            percent.setMin(0);
            percent.setMax(100);
            percent.setValue(0.0);

            type.setItems(CreateDiscountDTO.Type.values());
            type.setValue(CreateDiscountDTO.Type.VISIBLE);

            logic.setItems(CreateDiscountDTO.Logic.values());
            logic.setValue(CreateDiscountDTO.Logic.SINGLE);

            predicate.setItems("TOTAL", "QUANTITY", "CATEGORY", "PRODUCT");
            predicate.setVisible(true);
            valueWrapper.setVisible(true);

            logic.addValueChangeListener(e -> {
                boolean isSingle = logic.getValue() == CreateDiscountDTO.Logic.SINGLE;
                predicate.setVisible(isSingle);
                valueWrapper.setVisible(isSingle);
                addSubBtn.setVisible(!isSingle);
            });

            predicate.addValueChangeListener(e -> renderValueInput());
            renderValueInput();

            addSubBtn.addClickListener(e -> {
                DiscountFormEditor sub = new DiscountFormEditor();
                sub.logic.setValue(CreateDiscountDTO.Logic.SINGLE); // always SINGLE
                sub.logic.setReadOnly(true); // force SINGLE
                sub.addSubBtn.setVisible(false); // prevent nesting
                subEditors.add(sub);
            });

            HorizontalLayout topRow = new HorizontalLayout(name, percent, type, logic);
            HorizontalLayout condRow = new HorizontalLayout(predicate, valueWrapper);

            add(topRow, condRow, addSubBtn, subEditors);
        }

        private void renderValueInput() {
            valueWrapper.removeAll();
            String selected = predicate.getValue();
            if (selected == null)
                return;
            switch (selected) {
                case "TOTAL", "QUANTITY" -> {
                    NumberField num = new NumberField("Value");
                    num.setMin(0);
                    valueWrapper.add(num);
                }
                case "CATEGORY" -> {
                    ComboBox<Category> c = new ComboBox<>("Category");
                    c.setItems(Category.values());
                    valueWrapper.add(c);
                }
                case "PRODUCT" -> {
                    ComboBox<ItemStoreDTO> prod = new ComboBox<>("Product");
                    prod.setItems(currentProducts.keySet());
                    prod.setItemLabelGenerator(ItemStoreDTO::getProductName);
                    valueWrapper.add(prod);
                }
            }
        }

        public CreateDiscountDTO buildDTO() {
            String cond = "";
            if (logic.getValue() == CreateDiscountDTO.Logic.SINGLE) {
                String val = valueWrapper.getChildren()
                        .filter(c -> c instanceof HasValue)
                        .map(c -> ((HasValue<?, ?>) c).getValue())
                        .filter(Objects::nonNull)
                        .map(v -> {
                            if (predicate.getValue().equals("PRODUCT") && v instanceof ItemStoreDTO dto)
                                return dto.getProductId() + ""; // or any unique identifier
                            return v.toString();
                        })

                        .map(v -> v.endsWith(".0") ? v.substring(0, v.length() - 2) : v)
                        .findFirst().orElse("");

                cond = switch (predicate.getValue()) {
                    case "TOTAL" -> "TOTAL>" + val;
                    case "QUANTITY" -> "QUANTITY>" + val;
                    case "CATEGORY" -> "CATEGORY:" + val.toUpperCase();
                    case "PRODUCT" -> "ITEM:" + val;
                    default -> "";
                };
            }

            List<CreateDiscountDTO> subDiscounts = subEditors.getChildren()
                    .filter(c -> c instanceof DiscountFormEditor)
                    .map(c -> ((DiscountFormEditor) c).buildDTO())
                    .toList();

            return new CreateDiscountDTO(
                    name.getValue(),
                    percent.getValue() / 100.0,
                    type.getValue(),
                    cond,
                    logic.getValue(),
                    subDiscounts);
        }
    }

}