package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

@Route
@CssImport("./Theme/main-layout.css")
@JsModule("./notification.js")
public class MainLayout extends AppLayout {

    private HorizontalLayout buttonColumn;

    public MainLayout() {
        addClassName("main-layout");

        createHeader();         // Header עליון
    }

    
    private void createHeader() {
        // טקסט בצד שמאל
        H1 logo = new H1("🛒 Click Market");
        logo.addClassName("market-title");

        Paragraph subtitle = new Paragraph(
                "Welcome to our market. We bring the best stores and products to your fingertips.\n"
                        + "Join us and be an owner of your own store in a few clicks.");
        subtitle.addClassName("market-subtitle");

        VerticalLayout titleLayout = new VerticalLayout(logo, subtitle);
        titleLayout.setAlignItems(FlexComponent.Alignment.START);
        titleLayout.addClassName("header-title");

        // כפתורים בצד ימין
        buttonColumn = new HorizontalLayout();
        buttonColumn.addClassName("header-buttons");
        buttonColumn.setAlignItems(FlexComponent.Alignment.CENTER);

        RouterLink myCart = new RouterLink("My Cart", MyCartView.class);
        RouterLink homePage = new RouterLink("Home Page", HomePage.class);
        myCart.addClassName("right-button");
        homePage.addClassName("right-button");
        buttonColumn.add(myCart, homePage);

        String userType = (String) VaadinSession.getCurrent().getAttribute("user-type");
        if (userType == null || userType.equals("guest")) {
           RouterLink login = new RouterLink("Login", LoginView.class);
            login.addClassName("right-button");
            buttonColumn.add(login);
        }

        // שילוב לתוך header אחד
        HorizontalLayout fullHeader = new HorizontalLayout(titleLayout, buttonColumn);
        fullHeader.addClassName("app-header");
        fullHeader.setWidthFull();
        fullHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        fullHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        addToNavbar(fullHeader);
    }

     private void populateButtons() {
        buttonColumn.removeAll();

        RouterLink myCart = new RouterLink("My Cart", MyCartView.class);
        RouterLink homePage = new RouterLink("Home Page", HomePage.class);
        myCart.addClassName("right-button");
        homePage.addClassName("right-button");
        buttonColumn.add(myCart, homePage);

        String userType = (String) VaadinSession.getCurrent().getAttribute("user-type");
        if (userType == null || userType.equals("guest")) {
            RouterLink login = new RouterLink("Login", LoginView.class);
            login.addClassName("right-button");
            buttonColumn.add(login);
        }
    }

      public void refreshButtons() {
        if (buttonColumn != null) {
            buttonColumn.removeAll();
            populateButtons(); 
        }
    }

   
}