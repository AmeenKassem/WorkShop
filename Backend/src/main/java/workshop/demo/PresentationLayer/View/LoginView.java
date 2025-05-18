package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import ch.qos.logback.core.Layout;
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
        usernameField.setLabel("Username:");
        usernameField.addClassName("login-field");

        // Password
        passwordField.setLabel("Password:");
        passwordField.addClassName("login-field");

        // Login button
        loginButton.addClassName("login-button");

        loginButton.addClickListener(e -> {
            String user = getUsername();
            String pass = getPassword();
            System.out.println("Attempt login: " + user + "/" + pass);
            this.presenter.login();
        });

        // Form layout
        VerticalLayout form = new VerticalLayout(title, usernameField, passwordField, loginButton);
        form.addClassName("login-form");
        add(form);

    }

    public void refreshLayoutButtons() {
        getParent().ifPresent(parent -> {
            if (parent instanceof MainLayout layout) {
                layout.refreshButtons();
            }
        });
    }

    public String getUsername() {
        return usernameField.getValue();
    }

    public String getPassword() {
        return passwordField.getValue();
    }

    public void showSuccess(String msg) {
        Notification.show("✅ " + msg, 3000, Notification.Position.BOTTOM_CENTER);
    }

    public void showError(String msg) {
        Notification.show("❌ " + msg, 5000, Notification.Position.BOTTOM_CENTER);
    }
}
