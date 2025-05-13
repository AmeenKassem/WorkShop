package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

import workshop.demo.PresentationLayer.Presenter.InitPresenter;

@Route
@CssImport("./Theme/main-layout.css")
public class MainLayout extends AppLayout {

    private InitPresenter presenter;

    public MainLayout() {
        addClassName("main-layout");

        createHeader();
        this.presenter = new InitPresenter(this);

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        presenter.handleOnAttach("ws://localhost:8080/notifications",
                VaadinSession.getCurrent().getAttribute("current-user"));
    }

    // @Override
    // protected void onDetach(DetachEvent detachEvent) {
    //     presenter.handleOnDetach();
    // }
    private void createHeader() {
        H1 logo = new H1("🛒 MarketAppMarket App");
        RouterLink login = new RouterLink("Login", LoginView.class);
        RouterLink register = new RouterLink("Register", RegisterView.class);

        HorizontalLayout header = new HorizontalLayout(logo, login, register);
        header.addClassName("app-header");
        addToNavbar(header);
    }

}
