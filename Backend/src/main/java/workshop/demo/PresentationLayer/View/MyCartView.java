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
    private final VerticalLayout cartWrapper = new VerticalLayout();

    private final Button updateCartBtn = new Button("🔁 Update Cart", new Icon(VaadinIcon.REFRESH));
    private final Button continueShoppingBtn = new Button("⬅ Continue Shopping", new Icon(VaadinIcon.ARROW_LEFT));
    private final Button checkoutBtn = new Button("💳 Proceed to Checkout", new Icon(VaadinIcon.CREDIT_CARD));
    private final Button finalizeSpecialCarButton = new Button("✔ Finalize Special Cart", new Icon(VaadinIcon.CHECK));

    public MyCartView() {
        setSizeFull();
        addClassName("my-cart-view");
        presenter = new MyCartPresenter(this);

        setupHeader();
        setupActionButtons();
        setupCartSections();

        presenter.loadRegularCartItems();

        String userType = (String) VaadinSession.getCurrent().getAttribute("user-type");
        if (userType != null && userType.equals("user")) {
            presenter.loadSpecialCartItems();
            finalizeSpecialCarButton.setVisible(true);
            finalizeSpecialCarButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("purchase/special")));
        }
    }

    private void setupHeader() {
        H1 header = new H1("🏍️ My Cart");
        header.addClassName("cart-header");
        add(header);
    }

    private void setupCartSections() {
        regularItemsColumn.setClassName("cart-column");
        specialItemsColumn.setClassName("cart-column");

        regularItemsColumn.setWidthFull();
        specialItemsColumn.setWidthFull();

        VerticalLayout regularSection = new VerticalLayout(new H3("🛒 Regular Cart"), regularItemsColumn);
        VerticalLayout specialSection = new VerticalLayout(new H3("🔐 Special Cart"), specialItemsColumn);

        regularSection.setClassName("cart-section");
        specialSection.setClassName("cart-section");

        cartWrapper.setWidthFull();
        cartWrapper.setClassName("cart-wrapper");
        cartWrapper.add(regularSection, specialSection);

        add(cartWrapper);
    }

    private void setupActionButtons() {
        HorizontalLayout buttons = new HorizontalLayout(continueShoppingBtn, updateCartBtn, checkoutBtn,
                finalizeSpecialCarButton);
        finalizeSpecialCarButton.setVisible(false);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);
        buttons.setWidthFull();
        buttons.addClassName("cart-buttons");

        applyCartButtonStyle(continueShoppingBtn);
        applyCartButtonStyle(updateCartBtn);
        applyCartButtonStyle(checkoutBtn);
        applyCartButtonStyle(finalizeSpecialCarButton);

        continueShoppingBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));
        checkoutBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("purchase/regular")));

        add(buttons);
    }

    private void applyCartButtonStyle(Button btn) {
        btn.getStyle()
            .set("background-color", "#ff9900")
            .set("color", "white")
            .set("font-weight", "bold")
            .set("border-radius", "8px")
            .set("padding", "10px 16px")
            .set("min-width", "160px")
            .set("font-family", "'Segoe UI', sans-serif")
            .set("font-size", "0.9rem");
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

        card.add(createStyledLabel("🏪 Store: " + item.storeName));
        card.add(createStyledLabel("📦 Product: " + item.name));
        card.add(createStyledLabel("💰 Price: ₪" + item.price));
        card.add(createStyledLabel("📦 Quantity: " + item.quantity));
        card.add(createStyledLabel("🧮 Subtotal: ₪" + (item.price * item.quantity)));

        Button changeQtyBtn = new Button("Change Quantity", new Icon(VaadinIcon.PLUS));
        changeQtyBtn.addClickListener(e -> {
            TextField quantityField = new TextField("New Quantity");
            Button confirmBtn = new Button("Confirm");
            Dialog dialog = new Dialog(quantityField, confirmBtn);

            confirmBtn.addClickListener(ev -> {
                try {
                    int newQuantity = Integer.parseInt(quantityField.getValue());
                    presenter.updateQuantity(item.getId(), newQuantity);
                    dialog.close();
                } catch (NumberFormatException ex) {
                    NotificationView.showError("Please enter a valid number");
                }
            });

            dialog.open();
        });

        Button removeBtn = new Button("Remove", new Icon(VaadinIcon.TRASH));
        removeBtn.getStyle().set("color", "red");
        removeBtn.addClickListener(e -> presenter.removeFromCart(item.getId()));

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

        card.add(createStyledLabel("📦 Product: " + item.getProductName()));
        card.add(createStyledLabel("🏪 Store: " + item.storeName));
        card.add(createStyledLabel("🎯 Type: " + item.getType()));
        if(item.getType()==SpecialType.Auction){
            card.add(createStyledLabel("💰 my bid: " + (item.myBid)));
            card.add(createStyledLabel("🏁" + (item.isEnded() ? "Ended!" : " Ends at "+item.dateEnd)));
            card.add(createStyledLabel("🏆" + (item.onTop ? " You are on the top !" : " Some one bid with "+(item.maxBid))));
            // if(!item.onTop) card.add(createStyledLabel("💰 current max bid: " + (item.maxBid)));
        }
        return card;
    }

    private Span createStyledLabel(String text) {
        Span label = new Span(text);
        label.getStyle()
            .set("font-size", "0.95rem")
            .set("color", "#333")
            .set("font-family", "'Segoe UI', sans-serif");
        return label;
    }
}
