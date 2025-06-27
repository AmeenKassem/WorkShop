package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import workshop.demo.PresentationLayer.Presenter.AdminInitPresenter;

@Route("/admin/init")
public class AdminInitView extends VerticalLayout {

    private final AdminInitPresenter presenter;

    public AdminInitView() {
        this.presenter = new AdminInitPresenter(this);
        add(new H1("Admin Init Page"));

        TextField usernameField = new TextField("Admin Username");
        PasswordField passwordField = new PasswordField("Admin Password");
        TextField keyField = new TextField("Admin Key");

        Button initButton = new Button("Initialize System");
        initButton.addClickListener(event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();
            String key = keyField.getValue();

            presenter.initializeSystem(username, password, key);
        });
        String fieldWidth = "400px";

        usernameField.setWidth(fieldWidth);
        passwordField.setWidth(fieldWidth);
        keyField.setWidth(fieldWidth);
        initButton.setWidth(fieldWidth);
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(usernameField, passwordField, keyField, initButton);
    }
}
