package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import workshop.demo.PresentationLayer.Presenter.RegisterPresenter;

@Route(value = "register", layout = MainLayout.class)
@CssImport("./Theme/registerTheme.css")  //this links the CSS file
public class RegisterView extends VerticalLayout {

    private final TextField usernameField = new TextField("Username");
    private final PasswordField passwordField = new PasswordField("Password");
    private final TextField ageField = new TextField("Age");
    private final Button registerButton = new Button("Register");
    private RegisterPresenter presenter;

    public RegisterView() {
        addClassName("register-view");

        add(new H1("Create Account"), usernameField, passwordField, ageField, registerButton);

        presenter = new RegisterPresenter(this);

        registerButton.addClickListener(event -> presenter.register());
    }

    public String getUsername() {
        return usernameField.getValue();
    }

    public String getPassword() {
        return passwordField.getValue();
    }

    public int getAge() {
        try {
            return Integer.parseInt(ageField.getValue());
        } catch (NumberFormatException e) {
            return -1;//invalied
        }
    }

    public void showSuccess(String message) {
        Notification.show("✅ " + message, 3000, Notification.Position.MIDDLE);
    }

    public void showError(String message) {
        Notification.show("❌ " + message, 5000, Notification.Position.MIDDLE);
    }

}
