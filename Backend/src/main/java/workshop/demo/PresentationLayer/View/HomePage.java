package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
public class HomePage extends VerticalLayout {

    public HomePage() {
        add(new Label("Welcome to the Home Page!"));
    }

}
