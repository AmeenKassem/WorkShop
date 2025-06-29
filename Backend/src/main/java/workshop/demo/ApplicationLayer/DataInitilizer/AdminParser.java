package workshop.demo.ApplicationLayer.DataInitilizer;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AdminParser extends ManagerDataInit {
    protected void admin(List<String> construction) throws Exception {
        List<String> toSend = construction.subList(1, construction.size());
        switch (construction.get(0).toLowerCase()) {
            case "init":
                adminInit(toSend);
                break;
            default:
                log("syntax error on line " + line + " :Unkown (" + construction.get(0) + ") for admin");
                error = true;
                break;

        }
    }

    protected  void adminInit(List<String> construction) throws Exception {
        settings.markInitialized(construction.get(0), construction.get(1), construction.get(2));
        log("admin initilize the system successfully!");
    }
}
