package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import workshop.demo.PresentationLayer.Presenter.LoginPresenter;

@Route(value = "login", layout = MainLayout.class)
@CssImport("./Theme/loginTheme.css")
public class LoginView extends VerticalLayout {

    private final TextField usernameField = new TextField("Username");
    private final PasswordField passwordField = new PasswordField("Password");
    private final Button loginButton = new Button("Login");

    private final LoginPresenter presenter;

    public LoginView() {
        addClassName("login-view");

        presenter = new LoginPresenter(this);
        // Title
        H1 title = new H1("Login to your account");
        title.addClassName("login-title");

        // Username
        TextField username = new TextField("Username:");
        username.addClassName("login-field");

        // Password
        PasswordField password = new PasswordField("Password:");
        password.addClassName("login-field");

        // Login button
        Button loginButton = new Button("Login");
        loginButton.addClassName("login-button");

        loginButton.addClickListener(e -> {
            String user = username.getValue();
            String pass = password.getValue();

            // TODO: Call your presenter/service logic here
            this.presenter.login();
            System.out.println("Attempt login: " + user + "/" + pass);
        });

        // Form layout
        VerticalLayout form = new VerticalLayout(title, username, password, loginButton);
        form.addClassName("login-form");

        add(form);

    }

    public String getUsername() {
        return usernameField.getValue();
    }

    public String getPassword() {
        return passwordField.getValue();
    }

    public void showSuccess(String msg) {
        Notification.show("✅ " + msg, 3000, Notification.Position.MIDDLE);
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg, 5000, Notification.Position.MIDDLE);
    }
}
