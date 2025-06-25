package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("/404")
public class NotInitializedView extends VerticalLayout {

    public NotInitializedView() {
        H1 title = new H1("404 - Not Found");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(title);
    }

}
