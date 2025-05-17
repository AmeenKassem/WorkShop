package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "open-store", layout = MainLayout.class)
public class OpenStoreView extends VerticalLayout {

    public OpenStoreView() {
        add(new H1("Open Your Store"));
    }

}
