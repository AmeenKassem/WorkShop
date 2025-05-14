package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import workshop.demo.PresentationLayer.Presenter.MyStoresPresenter;

@Route(value = "my stores", layout = MainLayout.class)
@CssImport("./Theme/homePageTheme.css")
public class MyStoresView extends VerticalLayout {

    private final MyStoresPresenter presenter;

    public MyStoresView() {
        this.presenter = new MyStoresPresenter(this);
    }

}
