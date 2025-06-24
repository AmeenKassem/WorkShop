package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import workshop.demo.PresentationLayer.Presenter.OpenStorePresenter;

@Route(value = "open-store", layout = MainLayout.class)
@CssImport("./Theme/openStoreTheme.css")
public class OpenStoreView extends VerticalLayout {

    private OpenStorePresenter presenter;
    private final TextField storeNameField;
    private final TextField categoryField;

   public OpenStoreView() {
        this.presenter = new OpenStorePresenter(this);

        VerticalLayout formContainer = new VerticalLayout();
        formContainer.addClassName("open-store-container");

        storeNameField = new TextField("Store Name");
        storeNameField.setPlaceholder("Enter your store name");
        storeNameField.setWidthFull();

        categoryField = new TextField("Category");
        categoryField.setPlaceholder("Enter store category");
        categoryField.setWidthFull();

        Button openStoreButton = new Button("Open Store");
        openStoreButton.addClassName("open-store-button");
        openStoreButton.addClickListener(e -> presenter.openStore());

        formContainer.add(storeNameField, categoryField, openStoreButton);
        formContainer.setAlignItems(Alignment.CENTER);
        formContainer.setSpacing(true);
        formContainer.setWidth("400px");

        add(formContainer);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
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
