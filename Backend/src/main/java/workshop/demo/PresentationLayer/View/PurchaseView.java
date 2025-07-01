package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.PresentationLayer.Presenter.PurchasePresenter;

@Route(value = "purchase", layout = MainLayout.class)
@CssImport("./Theme/purchaseTheme.css")
public class PurchaseView extends VerticalLayout implements HasUrlParameter<String> {

    // Payment Details
    private final TextField cardNumber = new TextField("Card Number");
    private final TextField cardHolderName = new TextField("Cardholder Name");
    private final TextField expirationDate = new TextField("Expiration Date (MM/YY)");
    private final TextField cvv = new TextField("CVV");

    // Supply Details
    private final TextField address = new TextField("Address");
    private final TextField city = new TextField("City");
    private final TextField state = new TextField("State");
    private final TextField zipCode = new TextField("Zip Code");
    private final TextField name = new TextField("Receiver Name");
    private final TextField country = new TextField("Country");

    private final Button purchaseButton = new Button("Confirm Purchase");
    private final TextField couponCode = new TextField("Coupon Code (optional)");


    private final PurchasePresenter presenter;
    private String mode = "regular"; // Default

   public PurchaseView() {
        addClassName("purchase-view");
        presenter = new PurchasePresenter(this);

        H1 title = new H1("Complete Your Purchase");
        purchaseButton.addClassName("form-button");

        purchaseButton.addClickListener(e -> {
            if (anyFieldEmpty()) {
                NotificationView.showError("Please fill in all required fields.");
                return;
            }
            String coupon = couponCode.getValue();
            if ("special".equalsIgnoreCase(mode)) {
                presenter.submitSpecialPurchase(
                        getCardNumber(), getCardHolderName(), getExpirationDate(), getCvv(),
                        getAddress(), getCity(), getCountry(), getZipCode(), getName());
            } else {
                presenter.submitRegularPurchase(
                        getCardNumber(), getCardHolderName(), getExpirationDate(), getCvv(),
                        getAddress(), getCity(), getCountry(), getZipCode(), getName(),coupon);
            }
        });
        VerticalLayout formCard = new VerticalLayout(
                title,
                cardNumber,
                cardHolderName,
                expirationDate,
                cvv,
                address,
                city,
                state,
                zipCode,
                name,
                country,
                couponCode,
                purchaseButton);
        formCard.addClassName("form-card");

        add(formCard);

    }

    private boolean anyFieldEmpty() {
        return cardNumber.isEmpty() || cardHolderName.isEmpty() || expirationDate.isEmpty() || cvv.isEmpty()
                || address.isEmpty() || city.isEmpty() || zipCode.isEmpty() || name.isEmpty() || country.isEmpty();
    }

    // URL parameter handling
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null) {
            mode = parameter.trim().toLowerCase();
        }
    }

    // Getters
    public String getCardNumber() {
        return cardNumber.getValue();
    }

    public String getCardHolderName() {
        return cardHolderName.getValue();
    }

    public String getExpirationDate() {
        return expirationDate.getValue();
    }

    public String getCvv() {
        return cvv.getValue();
    }

    public String getAddress() {
        return address.getValue();
    }

    public String getCity() {
        return city.getValue();
    }

    public String getState() {
        return state.getValue();
    }

    public String getZipCode() {
        return zipCode.getValue();
    }

    public String getName() {
        return name.getValue();
    }

    public String getCountry() {
        return country.getValue();
    }

    public static void showReceiptDialog(ReceiptDTO[] receipts) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("🧾 My Purchase Receipts");
        dialog.getElement().getClassList().add("receipt-dialog");
        dialog.setWidth("600px"); 
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.addClassName("cart-section");
        wrapper.setSpacing(true);
        wrapper.setPadding(true);
        wrapper.setWidthFull();

        if (receipts.length == 0) {
            Paragraph empty = new Paragraph("No receipts available.");
            empty.getStyle().set("color", "#6b7280");
            wrapper.add(empty);
        } else {
            for (ReceiptDTO receipt : receipts) {
                VerticalLayout card = new VerticalLayout();
                card.addClassName("receipt-card");
                card.add(new Paragraph("📦 Store: " + receipt.getStoreName()));
                card.add(new Paragraph("📅 Date: " + receipt.getDate()));
                card.add(new Paragraph("💳 Total: $" + receipt.getFinalPrice()));
                wrapper.add(card);
            }
        }

        Button close = new Button("Close", e -> dialog.close());
        close.addClassName("right-button");
        close.getStyle().set("margin-top", "1rem");

        dialog.add(wrapper);
        dialog.getFooter().add(close);
        dialog.open();
    }
}
