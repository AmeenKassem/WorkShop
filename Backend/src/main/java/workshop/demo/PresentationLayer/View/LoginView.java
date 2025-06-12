package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import workshop.demo.PresentationLayer.Presenter.LoginPresenter;

@Route(value = "login", layout = MainLayout.class)
@CssImport("./Theme/loginTheme.css")
public class LoginView extends VerticalLayout {

    private final TextField usernameField = new TextField("Username");
    private final PasswordField passwordField = new PasswordField("Password");
    private final Button loginButton = new Button("Login");

    private final LoginPresenter presenter;
    private int failedAttempts = 0;

    public LoginView() {
        addClassName("login-view");

        presenter = new LoginPresenter(this);
        // Title
        H1 title = new H1("Login to your account");
        title.addClassName("login-title");
        // Fields
        usernameField.setPlaceholder("your name");
        passwordField.setPlaceholder("your password");
        usernameField.addClassName("login-field");
        passwordField.addClassName("login-field");
        // Live feedback: allow Enter key to submit
        usernameField.addFocusShortcut(Key.ENTER).listenOn(usernameField);
        passwordField.addFocusShortcut(Key.ENTER).listenOn(passwordField);

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
            // if (user == null || user.isBlank()) {
            //     NotificationView.showError("⚠️ Username cannot be empty.");
            //     return;
            // }
            // if (pass == null || pass.isBlank()) {
            //     NotificationView.showError("⚠️ Password cannot be empty.");
            //     return;
            // }
            // if (user == null || user.isBlank() || pass == null || pass.isBlank() || pass.length() < 5) {
            //     NotificationView.showError("Please fill in all fields");
            //     animateLoginDodge(); // 👈 make the button jump
            //     return;
            // }
            if (user.isBlank() || pass.isBlank()) {
                failedAttempts++;
                handleInvalidInput();
                return;
            }
            // Reset state
            loginButton.setText("Login");
            failedAttempts = 0;
            loginButton.getStyle().remove("--random-x");
            loginButton.getStyle().remove("--random-y");

            loginButton.setEnabled(false);
            presenter.login();
            loginButton.setEnabled(true);
        });
        // Register link
        RouterLink registerLink = new RouterLink("Don't have an account? Register here", RegisterView.class);
        registerLink.getStyle().set("margin-top", "1rem").set("font-size", "0.9rem");

        // Form layout
        VerticalLayout form = new VerticalLayout(title, usernameField, passwordField, loginButton, registerLink);
        form.addClassName("login-form");
        form.setSpacing(true);
        form.setWidthFull();

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

    private void animateLoginDodge() {
        String[] directions = {"shift-left", "shift-right", "shift-up", "shift-down"};
        int i = (int) (Math.random() * directions.length);
        String selected = directions[i];

        loginButton.addClassName(selected);

        // Remove class after animation finishes
        getUI().ifPresent(ui -> ui.access(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(400); // match animation duration
                    getUI().ifPresent(ui2 -> ui2.access(() -> {
                        loginButton.removeClassName(selected);
                    }));
                } catch (InterruptedException ignored) {
                }
            }).start();
        }));
    }

    private void handleInvalidInput() {
        String[] funTexts = {
            "Nope!", "Try again 👀", "Still wrong 🤭", "You're persistent 😅", "Give up?"
        };
        loginButton.setText(funTexts[failedAttempts % funTexts.length]);

        if (failedAttempts >= 4) {
            teleportButton();
        } else {
            animateLoginDodge();
        }

        NotificationView.showError("Please fill in all fields.");
    }

    private void teleportButton() {
        int randX = (int) (Math.random() * 3) - 1; // -1, 0, 1
        int randY = (int) (Math.random() * 3) - 1;

        loginButton.getElement().getStyle().set("--random-x", randX + "");
        loginButton.getElement().getStyle().set("--random-y", randY + "");
        loginButton.addClassName("random-move");

        getUI().ifPresent(ui -> ui.access(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    getUI().ifPresent(ui2 -> ui2.access(() -> loginButton.removeClassName("random-move")));
                } catch (InterruptedException ignored) {
                }
            }).start();
        }));
    }

    public void handleLoginError(int errorNumber) {
        switch (errorNumber) {
            case 1002 -> {
                NotificationView.showError("🤔 User not found!");
                teleportButton();
                loginButton.setText("Try again...");
            }
            case 1003 -> {
                NotificationView.showError("🔐 Wrong password!");
                animateLoginDodge();
                loginButton.setText("Wrong!");
            }
            case 9999 -> {
                NotificationView.showError("⚠️ Username or password is empty.");
                animateLoginDodge();
                loginButton.setText("😵");
            }
            default -> {
                NotificationView.showError("💥 Unexpected login error.");
                animateLoginDodge();
            }
        }
    }
}
