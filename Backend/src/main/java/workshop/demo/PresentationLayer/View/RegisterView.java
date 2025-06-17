package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import workshop.demo.PresentationLayer.Presenter.RegisterPresenter;

@Route(value = "register", layout = MainLayout.class)
@CssImport("./Theme/registerTheme.css")
public class RegisterView extends VerticalLayout {

    private final TextField usernameField = new TextField("Username:");
    private final PasswordField passwordField = new PasswordField("Password:");
    private final IntegerField ageField = new IntegerField("Age:");
    private final Button registerButton = new Button("Create Account");
    private final Span passwordFeedback = new Span();
    private RegisterPresenter presenter;

    public RegisterView() {
        addClassName("login-container");

        // Set up form layout
        VerticalLayout form = new VerticalLayout();
        form.addClassName("login-form");

        H1 title = new H1("Create your account");
        title.addClassName("login-title");

        usernameField.setPlaceholder("Choose a username");
        usernameField.addClassName("login-field");

        passwordField.setPlaceholder("Create a secure password");
        passwordField.addClassName("login-field");
        passwordFeedback.setText("Password must include at least 5 characters, one digit and one lowercase letter.");
        passwordFeedback.addClassName("password-feedback");
        passwordFeedback.setVisible(false);

        passwordField.addValueChangeListener(event -> {
            boolean valid = isPasswordValid(passwordField.getValue());
            passwordFeedback.setVisible(!valid);
        });

        ageField.setPlaceholder("Enter your age");
        ageField.setMin(18);
        ageField.setMax(120);
        ageField.setHelperText("You must be 18 or older");
        ageField.addClassName("login-field");

        registerButton.addClassName("login-button");
        registerButton.addClickListener(event -> presenter.register());

        form.add(title, usernameField, passwordField, passwordFeedback, ageField, registerButton);
        add(form);

        presenter = new RegisterPresenter(this);
    }

    public String getUsername() {
        return usernameField.getValue();
    }

    public String getPassword() {
        return passwordField.getValue();
    }

    public int getAge() {
        Integer value = ageField.getValue();
        return (value != null && value >= 0) ? value : -1;
    }

    public boolean isPasswordValid(String password) {
        return password != null
                && password.length() >= 5
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*");
    }
}
