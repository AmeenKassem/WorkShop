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
    private final HorizontalLayout cartWrapper = new HorizontalLayout();

    private final Button updateCartBtn = new Button("Update Cart", new Icon(VaadinIcon.REFRESH));
    private final Button continueShoppingBtn = new Button("Continue Shopping", new Icon(VaadinIcon.ARROW_LEFT));
    private final Button checkoutBtn = new Button("Proceed to Checkout", new Icon(VaadinIcon.CREDIT_CARD));
    private final Button finalizeSpecialCarButton = new Button("Finalize Special Cart", new Icon(VaadinIcon.CHECK));

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

        continueShoppingBtn.addClassName("cart-btn");
        updateCartBtn.addClassName("cart-btn");
        checkoutBtn.addClassName("cart-btn");
        finalizeSpecialCarButton.addClassName("cart-btn");
    }

    private void setupHeader() {
        Icon cartIcon = VaadinIcon.CART.create();
        cartIcon.setSize("32px");
        cartIcon.getStyle().set("color", "#ec4899").set("margin-right", "10px");
        cartIcon.getElement().setProperty("title", "Shopping Cart");

        Span titleText = new Span("Let’s Finalize Your Picks ✨");
        titleText.getStyle()
                .set("font-size", "2rem")
                .set("font-weight", "700")
                .set("font-family", "'Segoe UI', sans-serif")
                .set("color", "#be185d");

        HorizontalLayout headerLayout = new HorizontalLayout(cartIcon, titleText);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        headerLayout.setWidthFull();
        headerLayout.setFlexGrow(1, titleText);
        headerLayout.setFlexGrow(0, cartIcon);
        headerLayout.getStyle().set("flex-wrap", "wrap");
        headerLayout.addClassName("cart-header");

        add(headerLayout);
    }

    private void setupCartSections() {
        regularItemsColumn.setClassName("cart-column");
        specialItemsColumn.setClassName("cart-column");

        regularItemsColumn.setWidthFull();
        specialItemsColumn.setWidthFull();

        VerticalLayout regularSection = new VerticalLayout(new H3("\ud83d\uded2 Regular Cart"), regularItemsColumn);
        VerticalLayout specialSection = new VerticalLayout(new H3("\ud83d\udd10 Special Cart"), specialItemsColumn);

        regularSection.setClassName("cart-section");
        specialSection.setClassName("cart-section");

        cartWrapper.setWidthFull();
        cartWrapper.setSpacing(true);
        cartWrapper.setJustifyContentMode(JustifyContentMode.EVENLY);
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

        card.add(createInfoLine(VaadinIcon.SHOP, "Store: " + item.storeName));
        card.add(createInfoLine(VaadinIcon.PACKAGE, "Product: " + item.name));
        card.add(createInfoLine(VaadinIcon.MONEY, "Price: ₪" + item.price));
        card.add(createInfoLine(VaadinIcon.CART, "Quantity: " + item.quantity));
        card.add(createInfoLine(VaadinIcon.CALC_BOOK, "Subtotal: ₪" + (item.price * item.quantity)));

        Button changeQtyBtn = new Button("Change Quantity", new Icon(VaadinIcon.PLUS));
        Button removeBtn = new Button("Remove", new Icon(VaadinIcon.TRASH));
        removeBtn.getStyle().set("color", "red");

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

        removeBtn.addClickListener(e -> presenter.removeFromCart(item.getId()));

        HorizontalLayout buttonLayout = new HorizontalLayout(changeQtyBtn, removeBtn);
        buttonLayout.addClassName("btn-group");

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

        card.add(createInfoLine(VaadinIcon.PACKAGE, "Product: " + item.getProductName()));
        card.add(createInfoLine(VaadinIcon.SHOP, "Store: " + item.storeName));
        card.add(createInfoLine(VaadinIcon.TROPHY, "Type: " + item.getType()));
        card.add(createInfoLine(VaadinIcon.CART, "Quantity: " + item.quantity));

        if(item.getType()==SpecialType.Auction){
           card.add(createInfoLine(VaadinIcon.MONEY, "My bid: " + item.myBid));
            card.add(createInfoLine(VaadinIcon.CLOCK, item.isEnded() ? "Ended!" : "Ends at " + item.dateEnd));
            card.add(createInfoLine(VaadinIcon.ARROW_UP, item.onTop ? "You are on top!" : "Someone bid with " + item.maxBid));
           // card.add(createStyledLabel("\ud83c\udfc6" + (item.onTop ? " You are on the top !" : " Some one bid with "+(item.maxBid))));
        }
        if (item.getType() == SpecialType.Random) {
            card.add(createInfoLine(VaadinIcon.CLOCK, item.isEnded() ? "Ended at: " + item.dateEnd : "Ends at: " + item.dateEnd));
        }
        return card;
    }

    private Span createStyledLabel(String text) {
        Span label = new Span(text);
        label.addClassName("label");
        return label;
    }

    private Component createInfoLine(VaadinIcon icon, String text) {
        Icon iconComponent = icon.create();
        iconComponent.setSize("20px");
        iconComponent.getStyle().set("color", "#be185d");

        Span label = new Span(text);
        label.getElement().getClassList().add("info-line");

        HorizontalLayout line = new HorizontalLayout(iconComponent, label);
        line.setAlignItems(FlexComponent.Alignment.CENTER);
        line.setSpacing(true);
        line.getElement().getClassList().add("info-line");

        return line;
    }
}