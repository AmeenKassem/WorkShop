package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import workshop.demo.PresentationLayer.Presenter.MyCartPresenter;

@Route(value = "MyCart", layout = MainLayout.class)
public class MyCartView extends VerticalLayout {

    private final MyCartPresenter presenter;

    public MyCartView() {
        this.presenter = new MyCartPresenter(this);

        // Heading
        H2 title = new H2("Your Cart");

        // Button to go to Purchase Page
        Button goToPurchaseButton = new Button("Proceed to Checkout", new Icon(VaadinIcon.CREDIT_CARD));
        goToPurchaseButton.getStyle().set("margin-top", "20px");
        goToPurchaseButton.addClickListener(e -> 
            goToPurchasePage()
        );

        // Add components to the layout
        add(title, goToPurchaseButton);
    }

    private void goToPurchasePage() {
        getUI().ifPresent(ui -> ui.navigate("purchase"));
    }

}
