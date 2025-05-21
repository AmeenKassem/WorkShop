package workshop.demo.PresentationLayer.View;

import org.springframework.web.bind.annotation.PathVariable;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route(value = "store", layout = MainLayout.class)
public class StoreDetailsView extends VerticalLayout implements HasUrlParameter<Integer> {

    private int myStoreId;

    public StoreDetailsView() {
        add(new H1("Store Details "));

        // TODO: Call REST API or presenter to load store data and products
    }

    @Override
    public void setParameter(BeforeEvent event, Integer storeId) {
        if (storeId == null) {
            add(new Span("❌ No store ID provided."));
            return;
        }
        System.out.println("🚀 setParameter called with storeId = " + storeId);

        this.myStoreId = storeId;
        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            add(new Span("⚠️ You must be logged in to manage your store."));
            return;
        }

    }

}
