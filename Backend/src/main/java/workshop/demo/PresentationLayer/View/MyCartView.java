package workshop.demo.PresentationLayer.View;

import java.util.Locale.Category;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.SpecialCartItemDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.PresentationLayer.Presenter.MyCartPresenter;

@Route(value = "MyCart", layout = MainLayout.class)
@CssImport("./Theme/My-Cart.css")
public class MyCartView extends VerticalLayout {

    private final MyCartPresenter presenter;

    private final VerticalLayout normalCartLayout = new VerticalLayout();
    private final VerticalLayout specialCartLayout = new VerticalLayout();
    private final Button updateCartBtn = new Button("Update Cart", new Icon(VaadinIcon.REFRESH));
    private final Button continueShoppingBtn = new Button("Continue Shopping", new Icon(VaadinIcon.CART));
    private final Button checkoutBtn = new Button("Proceed to Checkout 🧾", new Icon(VaadinIcon.CREDIT_CARD));

    public MyCartView() {
        addClassName("cart-view");
        presenter = new MyCartPresenter(this);

        setupHeader();
        setupNormalCartSection();
        setupSpecialCartSection();
        setupActionButtons();

        // Use test data
        testLoadCartData();

        // Use real data instead by enabling:
        // presenter.loadCartItems();
    }

    private void setupHeader() {
        H1 title = new H1("🛍️ My Cart");
        title.addClassName("cart-title");
        add(title);
    }

    private void setupNormalCartSection() {
        H2 sectionTitle = new H2("🛒 Your Shopping Cart");
        normalCartLayout.setClassName("normal-cart");
        add(sectionTitle, normalCartLayout);
    }

    private void setupSpecialCartSection() {
        H2 sectionTitle = new H2("🔐 Your Special Cart");
        specialCartLayout.setClassName("special-cart");
        add(sectionTitle, specialCartLayout);
    }

    private void setupActionButtons() {
        checkoutBtn.addClickListener(e -> goToPurchasePage());

        HorizontalLayout buttons = new HorizontalLayout(updateCartBtn, continueShoppingBtn, checkoutBtn);
        buttons.setClassName("cart-buttons");
        add(buttons);
    }

    public void displayRegularItems(ItemCartDTO[] items) {
        normalCartLayout.removeAll();
        for (ItemCartDTO item : items) {
            normalCartLayout.add(createNormalCartItem(item));
        }
    }

    public void displaySpecialItems(SpecialCartItemDTO[] items) {
        specialCartLayout.removeAll();
        for (SpecialCartItemDTO item : items) {
            specialCartLayout.add(createSpecialCartItem(item));
        }
    }

    private Component createNormalCartItem(ItemCartDTO item) {
        HorizontalLayout container = new HorizontalLayout();
        container.setClassName("cart-item");

        Image image = new Image("https://via.placeholder.com/100", "Product Image");
        image.setWidth("100px");
        image.setHeight("100px");

        VerticalLayout details = new VerticalLayout();
        details.setClassName("cart-details");

        details.add(new Paragraph("🏪 Store: " + item.storeId));
        details.add(new Paragraph("📦 Product Name: " + item.name));
        details.add(new Paragraph("📝 Description: " + item.description));
        details.add(new Paragraph("💵 Price per unit: ₪" + item.price));
        details.add(new Paragraph("❄️ Quantity: " + item.quantity));
        details.add(new Paragraph("🔥 Subtotal: ₪" + (item.price * item.quantity)));

        container.add(image, details);
        return container;
    }

    private Component createSpecialCartItem(SpecialCartItemDTO item) {
        HorizontalLayout box = new HorizontalLayout();
        box.setClassName("special-item");

        // Style based on type
        if (item.getType() == SpecialType.Random) {
            box.addClassName("random-bg");
        } else if (item.getType() == SpecialType.Auction) {
            box.addClassName("auction-bg");
        } else if (item.getType() == SpecialType.BID) {
            box.addClassName("bid-bg");
        }

        Image image = new Image("https://via.placeholder.com/100", "Special Product");
        image.setWidth("100px");
        image.setHeight("100px");

        VerticalLayout details = new VerticalLayout();
        details.setClassName("special-details");

        details.add(new Paragraph("📦 Product: " + item.getProductName()));
        details.add(new Paragraph("🏪 Store ID: " + item.getStoreId()));
        details.add(new Paragraph("🧾 Type: " + item.getType()));
        details.add(new Paragraph("🏁 Ended: " + (item.isEnded() ? "Yes" : "No")));
        details.add(new Paragraph("🏆 You Won: " + (item.isWinner() ? "Yes" : "No")));

        box.add(image, details);
        return box;
    }

    private void goToPurchasePage() {
        getUI().ifPresent(ui -> ui.navigate("purchase"));
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg, 4000, Notification.Position.BOTTOM_CENTER);
    }

    public void showSuccess(String msg) {
        Notification.show("✅ " + msg, 3000, Notification.Position.BOTTOM_CENTER);
    }

    // 🔧 Test data method
    public void testLoadCartData() {
        // Create 3 normal items from 3 stores
        ItemCartDTO item1 = new ItemCartDTO(1,workshop.demo.DTOs.Category.HOME , 101, 2, 20, "Milk", "Fresh dairy milk", "Store A");
        ItemCartDTO item2 = new ItemCartDTO(2, workshop.demo.DTOs.Category.ELECTRONICS, 102, 1, 150, "Headphones", "Bluetooth over-ear", "Store B");
        ItemCartDTO item3 = new ItemCartDTO(3, workshop.demo.DTOs.Category.ELECTRONICS, 103, 3, 45, "Notebook", "A5 spiral notebook", "Store C");
        displayRegularItems(new ItemCartDTO[]{item1, item2, item3});

        // Create 3 special items
        SpecialCartItemDTO randomItem = new SpecialCartItemDTO();
        randomItem.setIds(10, 1001, 0, SpecialType.Random);
        randomItem.setValues("Mystery Box", false, false);

        SpecialCartItemDTO auctionItem = new SpecialCartItemDTO();
        auctionItem.setIds(11, 1002, 0, SpecialType.Auction);
        auctionItem.setValues("Antique Clock", true, true);

        SpecialCartItemDTO bidItem = new SpecialCartItemDTO();
        bidItem.setIds(12, 1003, 2001, SpecialType.BID);
        bidItem.setValues("Gaming Mouse", false, true);

        displaySpecialItems(new SpecialCartItemDTO[]{randomItem, auctionItem, bidItem});
    }
}
