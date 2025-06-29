package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import workshop.demo.PresentationLayer.Presenter.AdminInitPresenter;

@Route("/admin/init")
@CssImport("./Theme/admin-init-view.css")
public class AdminInitView extends VerticalLayout {

    private final AdminInitPresenter presenter;
    private final Div logOutput = new Div();
    private final Dialog logDialog = new Dialog();

    public AdminInitView() {
        this.presenter = new AdminInitPresenter(this);

        H1 title = new H1("🔐 Admin Init Panel");
        title.addClassName("main-title");

        TextField usernameField = new TextField("👤 Admin Username");
        PasswordField passwordField = new PasswordField("🔑 Admin Password");
        TextField keyField = new TextField("🧿 Admin Key");

        String fieldWidth = "400px";
        usernameField.setWidth(fieldWidth);
        passwordField.setWidth(fieldWidth);
        keyField.setWidth(fieldWidth);

        Button initButton = new Button("🚀 Initialize System", new Icon(VaadinIcon.COG));
        Button deleteButton = new Button("🗑️ Delete Data", new Icon(VaadinIcon.TRASH));
        Button initFromDIFButton = new Button("📁 Initialize from DIF", new Icon(VaadinIcon.FILE_TREE));

        initButton.setWidth(fieldWidth);
        deleteButton.setWidth(fieldWidth);
        initFromDIFButton.setWidth(fieldWidth);

        logOutput.setWidth("600px");
        logOutput.setHeight("300px");
        logOutput.addClassName("terminal-log");

        Icon closeIcon = new Icon(VaadinIcon.CLOSE);
        closeIcon.getStyle().set("cursor", "pointer");
        closeIcon.addClickListener(e -> logDialog.close());

        HorizontalLayout dialogHeader = new HorizontalLayout(closeIcon);
        dialogHeader.setWidthFull();
        dialogHeader.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout dialogContent = new VerticalLayout(dialogHeader, logOutput);
        dialogContent.setPadding(false);
        dialogContent.setSpacing(false);
        dialogContent.setAlignItems(Alignment.CENTER);

        logDialog.add(dialogContent);
        logDialog.setModal(true);
        logDialog.setDraggable(true);
        logDialog.setResizable(true);

        VerticalLayout formLayout = new VerticalLayout(
                usernameField, passwordField, keyField,
                initButton, deleteButton, initFromDIFButton
        );
        formLayout.setAlignItems(Alignment.CENTER);
        formLayout.setSpacing(true);

        VerticalLayout panel = new VerticalLayout(title, formLayout);
        panel.addClassName("admin-init-panel");

        initButton.addClickListener(event -> {
            presenter.initializeSystem(usernameField.getValue(), passwordField.getValue(), keyField.getValue());
        });

        deleteButton.addClickListener(event -> {
            presenter.deleteData(usernameField.getValue(), passwordField.getValue(), keyField.getValue());
        });

        initFromDIFButton.addClickListener(event -> {
            presenter.initDataFromDIF(usernameField.getValue(), passwordField.getValue(), keyField.getValue());
            logDialog.open();
        });

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);
        setPadding(true);
        add(panel);
    }

    public void showDIFLogs(String logs) {
        StringBuilder highlighted = new StringBuilder();
        for (String line : logs.split("\n")) {
            String safe = line.replace("<", "&lt;").replace(">", "&gt;");
            String styleClass = "";
            String lower = line.toLowerCase();
            if (lower.contains("error") || lower.contains("fail")) {
                styleClass = "log-error";
            } else if (lower.contains("warn")) {
                styleClass = "log-warn";
            } else if (lower.contains("success") || lower.contains("done")) {
                styleClass = "log-success";
            }
            highlighted.append("<div class='").append(styleClass).append("'>").append(safe).append("</div>");
        }
        logOutput.getElement().setProperty("innerHTML", highlighted.toString());
    }
}
