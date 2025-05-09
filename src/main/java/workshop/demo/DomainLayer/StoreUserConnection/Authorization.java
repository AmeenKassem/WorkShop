package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Authorization {
    private static final Logger logger = LoggerFactory.getLogger(Authorization.class);

    private Map<Permission, Boolean> myAutho;

    public Authorization() {
        this.myAutho = new HashMap<>();
        fillAuth();

    }

    private void fillAuth() {
        for (Permission permission : Permission.values()) {
            myAutho.put(permission, false);
        }
        logger.debug("All permissions set to false by default");

    }

    // might delete later
    public void addAuthorization(List<Permission> toAdd) {
        logger.debug("addAuthorization called with {} permissions", toAdd.size());
        for (Permission permission : toAdd) {
            myAutho.put(permission, true);
            logger.debug("Permission {} set to true", permission);
        }
    }

    // explaintion:i.e., if the permission is already set to true, change it to
    // false, and vice versa
    public void updateAuthorixation(List<Permission> toUpdate) {
        logger.debug("updateAuthorixation called with {} permissions", toUpdate.size());

        for (Permission permission : toUpdate) {
            if (myAutho.containsKey(permission)) {
                boolean currentValue = myAutho.get(permission);
                myAutho.put(permission, !currentValue);
                logger.debug("Permission {} toggled from {} to {}", permission, currentValue, !currentValue);

            }
        }
    }

    public boolean hasAutho(Permission per) {
        boolean res = myAutho.get(per);
        logger.debug("hasAutho check for {}: {}", per, res);
        return res;

    }

    public Map<Permission, Boolean> getMyAutho() {
        logger.debug("getMyAutho called");

        return myAutho;
    }

    public void setMyAutho(Map<Permission, Boolean> myAutho) {
        this.myAutho = myAutho;
        logger.debug("setMyAutho called, map updated");

    }
}
