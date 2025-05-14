package workshop.demo.PresentationLayer.View;

import org.springframework.web.bind.annotation.PathVariable;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("store/:id")
public class StoreDetailsView extends VerticalLayout {

    public StoreDetailsView(@PathVariable("id") int storeId) {
        add(new H1("Store Details for ID: " + storeId));

        // TODO: Call REST API or presenter to load store data and products
    }

}
