package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import workshop.demo.PresentationLayer.Presenter.MyCartPresenter;

@Route(value = "MyCart", layout = MainLayout.class)
public class MyCartView extends VerticalLayout {

    private final MyCartPresenter presenter;

    public MyCartView() {
        this.presenter = new MyCartPresenter(this);
    }

}
