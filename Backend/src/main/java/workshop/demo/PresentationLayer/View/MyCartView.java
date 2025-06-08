package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.SpecialCartItemDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.PresentationLayer.Presenter.MyCartPresenter;

@Route(value = "MyCart", layout = MainLayout.class)
@CssImport("./Theme/My-Cart.css")
public class MyCartView extends VerticalLayout {

    private final MyCartPresenter presenter;
    private final VerticalLayout regularItemsColumn = new VerticalLayout();
    private final VerticalLayout specialItemsColumn = new VerticalLayout();
    private final HorizontalLayout cartContainer = new HorizontalLayout();

    private final Button updateCartBtn = new Button("🔁 Update Cart", new Icon(VaadinIcon.REFRESH));
    private final Button continueShoppingBtn = new Button("⬅ Continue Shopping", new Icon(VaadinIcon.ARROW_LEFT));
    private final Button checkoutBtn = new Button("💳 Proceed to Checkout", new Icon(VaadinIcon.CREDIT_CARD));
    private final Button finalizeSpecialCarButton = new Button("Finalize Special Cart", new Icon(VaadinIcon.CHECK));

    public MyCartView() {
        setSizeFull();
        addClassName("my-cart-view");
        presenter = new MyCartPresenter(this);

        setupHeader();
        setupCartColumns();
        setupActionButtons();

        presenter.loadRegularCartItems();
        // loadTestData(); // For testing purposes, remove in production

        String userType = (String) VaadinSession.getCurrent().getAttribute("user-type");
        if (userType != null && userType.equals("user")) {
            presenter.loadSpecialCartItems();
            finalizeSpecialCarButton.setVisible(true);
            finalizeSpecialCarButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("purchase/special")));
        }
    }

    private void loadTestData() {
        // Sample Regular Items
        ItemCartDTO item1 = new ItemCartDTO(1, 2, 101, 10, "Bananas", "Fresh bananas", Category.Beauty);
        ItemCartDTO item2 = new ItemCartDTO(2, 1, 102, 199, "Bluetooth Speaker", "Loud and portable",
                Category.Clothing);
        ItemCartDTO item3 = new ItemCartDTO(3, 3, 103, 15, "Pillow", "Soft pillow", Category.Home);

        displayRegularItems(new ItemCartDTO[] { item1, item2, item3 });

        // Sample Special Items
        SpecialCartItemDTO special1 = new SpecialCartItemDTO();
        special1.setIds(201, 1001, 0, SpecialType.Random);
        special1.setValues("Mystery Box", false, false);

        SpecialCartItemDTO special2 = new SpecialCartItemDTO();
        special2.setIds(202, 1002, 0, SpecialType.Auction);
        special2.setValues("Rare Coin", true, true);

        SpecialCartItemDTO special3 = new SpecialCartItemDTO();
        special3.setIds(203, 1003, 10001, SpecialType.BID);
        special3.setValues("Gaming Chair", false, true);

        displaySpecialItems(new SpecialCartItemDTO[] { special1, special2, special3 });
    }

    private void setupHeader() {
        H1 header = new H1("🛍️ My Cart");
        header.addClassName("cart-header");
        add(header);
    }

    private void setupCartColumns() {
        // Style and scroll logic
        regularItemsColumn.setClassName("cart-column");
        regularItemsColumn.getStyle().set("overflow-y", "auto");
        regularItemsColumn.setHeight("500px");
        regularItemsColumn.setWidth("100%");

        specialItemsColumn.setClassName("cart-column");
        specialItemsColumn.getStyle().set("overflow-y", "auto");
        specialItemsColumn.setHeight("500px");
        specialItemsColumn.setWidth("100%");

        VerticalLayout regularWrapper = new VerticalLayout(new H3("🛒 Regular Cart"), regularItemsColumn);
        VerticalLayout specialWrapper = new VerticalLayout(new H3("🔐 Special Cart"), specialItemsColumn);

        regularWrapper.setWidth("50%");
        specialWrapper.setWidth("50%");

        cartContainer.setWidthFull();
        cartContainer.setSpacing(true);
        cartContainer.add(regularWrapper, specialWrapper);

        add(cartContainer);
    }

    private void setupActionButtons() {
        HorizontalLayout buttons = new HorizontalLayout(continueShoppingBtn, updateCartBtn, checkoutBtn,
                finalizeSpecialCarButton);
        finalizeSpecialCarButton.setVisible(false); // Initially hidden
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);
        buttons.setWidthFull();
        buttons.addClassName("cart-buttons");

        continueShoppingBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));
        checkoutBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("purchase/regular")));

        add(buttons);
    }

    public void displayRegularItems(ItemCartDTO[] items) {
        regularItemsColumn.removeAll();
        for (ItemCartDTO item : items) {
            regularItemsColumn.add(createRegularItemCard(item));
        }
    }

    public void displaySpecialItems(SpecialCartItemDTO[] items) {
        specialItemsColumn.removeAll();
        for (SpecialCartItemDTO item : items) {
            specialItemsColumn.add(createSpecialItemCard(item));
        }
    }

    private Component createRegularItemCard(ItemCartDTO item) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("item-card");

        card.add(new Paragraph("🏪 Store: " + item.storeId));
        card.add(new Paragraph("📦 Product: " + item.name));
        card.add(new Paragraph("💰 Price: ₪" + item.price));
        card.add(new Paragraph("📦 Quantity: " + item.quantity));
        card.add(new Paragraph("🧮 Subtotal: ₪" + (item.price * item.quantity)));

        // Change Quantity button
        Button changeQtyBtn = new Button("Change Quantity", new Icon(VaadinIcon.PLUS));
        changeQtyBtn.addClickListener(e -> {
            // You can use a dialog or input prompt for actual input
            TextField quantityField = new TextField("New Quantity");
            Button confirmBtn = new Button("Confirm");
            Dialog dialog = new Dialog(quantityField, confirmBtn);

            confirmBtn.addClickListener(ev -> {
                try {
                    int newQuantity = Integer.parseInt(quantityField.getValue());
                    presenter.updateQuantity(item.productId, newQuantity); // <-- Presenter method
                    dialog.close();
                } catch (NumberFormatException ex) {
                    NotificationView.showError("Please enter a valid number");
                }
            });

            dialog.open();
        });

        // Remove button
        Button removeBtn = new Button("Remove", new Icon(VaadinIcon.TRASH));
        removeBtn.getStyle().set("color", "red");
        removeBtn.addClickListener(e -> {
            presenter.removeFromCart(item.productId); // <-- Presenter method
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(changeQtyBtn, removeBtn);
        card.add(buttonLayout);

        return card;
    }

    private Component createSpecialItemCard(SpecialCartItemDTO item) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("item-card");

        if (item.getType() == SpecialType.Auction) {
            card.addClassName("auction-bg");
        } else if (item.getType() == SpecialType.Random) {
            card.addClassName("random-bg");
        } else if (item.getType() == SpecialType.BID) {
            card.addClassName("bid-bg");
        }

        card.add(new Paragraph("📦 Product: " + item.getProductName()));
        card.add(new Paragraph("🏪 Store ID: " + item.getStoreId()));
        card.add(new Paragraph("🎯 Type: " + item.getType()));
        card.add(new Paragraph("🏁 Ended: " + (item.isEnded() ? "Yes" : "No")));
        card.add(new Paragraph("🏆 You Won: " + (item.isWinner() ? "Yes" : "No")));

        // Button viewBtn = new Button("View / Edit", new Icon(VaadinIcon.SEARCH));
        // viewBtn.addClickListener(e -> getUI().ifPresent(ui ->
        // ui.navigate("SpecialProductDetails/" + item.getProductId())));

        // card.add(viewBtn);
        return card;
    }
}
