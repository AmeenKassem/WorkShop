package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route(value = "", layout = MainLayout.class)
@CssImport("./Theme/homePageTheme.css")
public class HomePage extends VerticalLayout {

    public HomePage() {
        setSizeFull();
        setSpacing(false);
        setPadding(true);
        setAlignItems(Alignment.START);
        addClassName("home-view");

        RouterLink login = new RouterLink("Login", LoginView.class);
        RouterLink register = new RouterLink("Register", RegisterView.class);
        //change it when making the cart page
        RouterLink myCart = new RouterLink("My Cart", MyCartView.class);
        login.addClassName("home-button");
        register.addClassName("home-button");
        myCart.addClassName("home-button");

        HorizontalLayout buttonRow = new HorizontalLayout(login, register, myCart);
        buttonRow.setWidthFull(); // take full width
        buttonRow.setJustifyContentMode(JustifyContentMode.START); // force align left
        buttonRow.addClassName("button-row");

        // === Placeholder for featured stores ===
        Div storeArea = new Div(new Text("🔥 Featured Stores will be displayed here soon..."));
        storeArea.addClassName("store-area");

        add(buttonRow, storeArea);
    }

}
