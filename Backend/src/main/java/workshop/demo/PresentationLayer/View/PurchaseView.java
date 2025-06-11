// package workshop.demo.PresentationLayer.View;

// import com.vaadin.flow.component.button.Button;
// import com.vaadin.flow.component.dependency.CssImport;
// import com.vaadin.flow.component.dialog.Dialog;
// import com.vaadin.flow.component.html.Div;
// import com.vaadin.flow.component.html.H1;
// import com.vaadin.flow.component.html.H3;
// import com.vaadin.flow.component.html.Paragraph;
// import com.vaadin.flow.component.orderedlayout.VerticalLayout;
// import com.vaadin.flow.component.textfield.TextField;
// import com.vaadin.flow.router.Route;

// import workshop.demo.DTOs.ReceiptDTO;
// import workshop.demo.DTOs.ReceiptProduct;
// import workshop.demo.PresentationLayer.Presenter.PurchasePresenter;

// @Route(value = "purchase", layout = MainLayout.class)
// @CssImport("./Theme/purchaseTheme.css")
// public class PurchaseView extends VerticalLayout {

//     // Payment Details
//     private final TextField cardNumber = new TextField("Card Number");
//     private final TextField cardHolderName = new TextField("Cardholder Name");
//     private final TextField expirationDate = new TextField("Expiration Date (MM/YY)");
//     private final TextField cvv = new TextField("CVV");

//     // Supply Details
//     private final TextField address = new TextField("Address");
//     private final TextField city = new TextField("City");
//     private final TextField state = new TextField("State");
//     private final TextField zipCode = new TextField("Zip Code");

//     private final Button purchaseButton = new Button("Confirm Purchase");


//     private final PurchasePresenter presenter;

//     public PurchaseView() {
//         addClassName("purchase-view");
//         presenter = new PurchasePresenter(this);

//         H1 title = new H1("Complete Your Purchase");

//         purchaseButton.addClickListener(e -> {
//             String cardNum = cardNumber.getValue();
//             String cardName = cardHolderName.getValue();
//             String expiry = expirationDate.getValue();
//             String cvvVal = cvv.getValue();
//             String addr = address.getValue();
//             String cityVal = city.getValue();
//             String stateVal = state.getValue();
//             String zip = zipCode.getValue();

//             if (cardNum.isEmpty() || cardName.isEmpty() || expiry.isEmpty() || cvvVal.isEmpty()
//                     || addr.isEmpty() || cityVal.isEmpty() || stateVal.isEmpty() || zip.isEmpty()) {
//                 NotificationView.showError("Please fill in all required fields.");
//                 return;
//             }

//             presenter.submitPurchase(
//                     getCardNumber(),
//                     getCardHolderName(),
//                     getExpirationDate(),
//                     getCvv(),
//                     getAddress(),
//                     getCity(),
//                     getState(),
//                     getZipCode());
//         });

//         add(
//                 title,
//                 cardNumber,
//                 cardHolderName,
//                 expirationDate,
//                 cvv,
//                 address,
//                 city,
//                 state,
//                 zipCode,
//                 purchaseButton);
//     }

//     // Getters
//     public String getCardNumber() {
//         return cardNumber.getValue();
//     }

//     public String getCardHolderName() {
//         return cardHolderName.getValue();
//     }

//     public String getExpirationDate() {
//         return expirationDate.getValue();
//     }

//     public String getCvv() {
//         return cvv.getValue();
//     }

//     public String getAddress() {
//         return address.getValue();
//     }

//     public String getCity() {
//         return city.getValue();
//     }

//     public String getState() {
//         return state.getValue();
//     }

//     public String getZipCode() {
//         return zipCode.getValue();
//     }

//     public static void showReceiptDialog(ReceiptDTO[] receipts) {
//         System.out.println("Showing receipt dialog with " + receipts.length + " receipts.");
//         Dialog dialog = new Dialog();
//         dialog.setCloseOnEsc(true);
//         dialog.setCloseOnOutsideClick(false);

//         Div content = new Div();
//         content.getStyle().set("max-height", "400px").set("overflow", "auto");

//         for (ReceiptDTO receipt : receipts) {
//             Div receiptDiv = new Div();
//             receiptDiv.getStyle().set("border", "1px solid #ccc").set("padding", "10px").set("margin", "10px");

//             receiptDiv.add(new H3("Store: " + receipt.getStoreName()));
//             receiptDiv.add(new Paragraph("Date: " + receipt.getDate()));
//             receiptDiv.add(new Paragraph("Total: $" + receipt.getFinalPrice()));

//             if (receipt.getProductsList() != null) {
//                 for (ReceiptProduct product : receipt.getProductsList()) {
//                     receiptDiv.add(new Paragraph("- " + product.getProductName() +
//                             " (x" + product.getQuantity() + "), $" + product.getPrice()));
//                 }
//             }

//             content.add(receiptDiv);
//         }

//         Button closeBtn = new Button("Close", e -> dialog.close());
//         closeBtn.getStyle().set("margin-top", "10px");
//         dialog.add(content, closeBtn);
//         dialog.open();
//     }
// }
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

    private final Button purchaseButton = new Button("Confirm Purchase");

    private final PurchasePresenter presenter;
    private String mode = "regular"; // Default

    public PurchaseView() {
        addClassName("purchase-view");
        presenter = new PurchasePresenter(this);

        H1 title = new H1("Complete Your Purchase");

        purchaseButton.addClickListener(e -> {
            if (anyFieldEmpty()) {
                NotificationView.showError("Please fill in all required fields.");
                return;
            }

            if ("special".equalsIgnoreCase(mode)) {
                presenter.submitSpecialPurchase(getCardNumber(), getCardHolderName(), getExpirationDate(), getCvv(),
                        getAddress(), getCity(), getState(), getZipCode());
            } else {
                presenter.submitRegularPurchase(getCardNumber(), getCardHolderName(), getExpirationDate(), getCvv(),
                        getAddress(), getCity(), getState(), getZipCode());
            }
        });

        add(
                title,
                cardNumber,
                cardHolderName,
                expirationDate,
                cvv,
                address,
                city,
                state,
                zipCode,
                purchaseButton);
    }

    private boolean anyFieldEmpty() {
        return cardNumber.isEmpty() || cardHolderName.isEmpty() || expirationDate.isEmpty() || cvv.isEmpty()
                || address.isEmpty() || city.isEmpty() || state.isEmpty() || zipCode.isEmpty();
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

    public static void showReceiptDialog(ReceiptDTO[] receipts) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        Div content = new Div();
        content.getStyle().set("max-height", "400px").set("overflow", "auto");

        for (ReceiptDTO receipt : receipts) {
            Div receiptDiv = new Div();
            receiptDiv.getStyle().set("border", "1px solid #ccc").set("padding", "10px").set("margin", "10px");

            receiptDiv.add(new H3("Store: " + receipt.getStoreName()));
            receiptDiv.add(new Paragraph("Date: " + receipt.getDate()));
            receiptDiv.add(new Paragraph("Total: $" + receipt.getFinalPrice()));

            if (receipt.getProductsList() != null) {
                for (ReceiptProduct product : receipt.getProductsList()) {
                    receiptDiv.add(new Paragraph("- " + product.getProductName() +
                            " (x" + product.getQuantity() + "), $" + product.getPrice()));
                }
            }

            content.add(receiptDiv);
        }

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.getStyle().set("margin-top", "10px");
        dialog.add(content, closeBtn);
        dialog.open();
    }
}
