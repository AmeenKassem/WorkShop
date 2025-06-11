package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.PresentationLayer.Presenter.ManagePolicyPresenter;

@Route(value = "manage-policy", layout = MainLayout.class)
public class ManagePolicyView extends VerticalLayout implements HasUrlParameter<Integer> {

    private int storeId;
    private final ManagePolicyPresenter presenter;

    public ManagePolicyView() {
        this.presenter = new ManagePolicyPresenter(this);
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.START);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer storeId) {
        if (storeId == null) {
            add(new Span("❌ No store ID provided."));
            return;
        }
        this.storeId = storeId;

        String token = (String) VaadinSession.getCurrent().getAttribute("auth-token");
        if (token == null) {
            return;
        }
    }

}
