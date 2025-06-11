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
@CssImport("./Theme/registerTheme.css")  //this links the CSS file
public class RegisterView extends VerticalLayout {

    private final TextField usernameField = new TextField("Username");
    private final PasswordField passwordField = new PasswordField("Password");
    private final IntegerField ageField = new IntegerField("Age");
    private final Button registerButton = new Button("Register");
    private final Span passwordFeedback = new Span();
    private RegisterPresenter presenter;

    public RegisterView() {
        addClassName("register-view");
        // Set placeholders and constraints
        usernameField.setPlaceholder("Enter your username");
        passwordField.setPlaceholder("Enter your password");
        passwordFeedback.getStyle().set("color", "red");
        passwordFeedback.setText("Password must be 8+ chars, include uppercase, lowercase, and a number.");
        passwordFeedback.setVisible(false);

        passwordField.addValueChangeListener(event -> {
            boolean valid = isPasswordValid(passwordField.getValue());
            passwordFeedback.setVisible(!valid);
        });

        ageField.setPlaceholder("Enter your age");
        ageField.setMin(18);
        ageField.setMax(120);
        ageField.setHelperText("You must be 18 or older");

        add(new H1("Create Account"), usernameField, passwordField, passwordFeedback, ageField, registerButton);

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
        Integer value = ageField.getValue();
        return (value != null && value >= 0) ? value : -1;
    }

    public boolean isPasswordValid(String password) {
        return password != null
                && password.length() >= 5
                && password.matches(".*[a-z].*")
                //&& password.matches(".*[A-Z].*")
                && password.matches(".*\\d.*");
    }

}
