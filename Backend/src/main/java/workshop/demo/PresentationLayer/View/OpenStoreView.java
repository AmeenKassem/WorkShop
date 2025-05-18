package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import workshop.demo.PresentationLayer.Presenter.OpenStorePresenter;

@Route(value = "open-store", layout = MainLayout.class)
public class OpenStoreView extends VerticalLayout {

    private OpenStorePresenter presenter;
    private final TextField storeNameField;
    private final TextField categoryField;

    public OpenStoreView() {
        this.presenter = new OpenStorePresenter(this);

        storeNameField = new TextField("Store Name");
        categoryField = new TextField("Category");

        Button openStoreButton = new Button("Open Store");
        openStoreButton.addClickListener(e -> presenter.openStore());

        add(storeNameField, categoryField, openStoreButton);
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
    }

    public String getStoreName() {
        return storeNameField.getValue();
    }

    public String getCategory() {
        return categoryField.getValue();
    }

    public void showSuccess(String msg) {
        Notification.show("✅ " + msg, 3000, Notification.Position.BOTTOM_CENTER);
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg, 5000, Notification.Position.BOTTOM_CENTER);
    }
}
