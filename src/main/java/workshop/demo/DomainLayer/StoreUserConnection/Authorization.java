package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Authorization {

    private Map<Permission, Boolean> myAutho;

    public Authorization() {
        this.myAutho = new HashMap<>();
        fillAuth();
    }

    private void fillAuth() {
        for (Permission permission : Permission.values()) {
            myAutho.put(permission, false);
        }
    }

    //might delete later
    public void addAuthorization(List<Permission> toAdd) {
        for (Permission permission : toAdd) {
            myAutho.put(permission, true);
        }
    }

    //explaintion:i.e., if the permission is already set to true, change it to false, and vice versa
    public void updateAuthorixation(List<Permission> toUpdate) {
        for (Permission permission : toUpdate) {
            if (myAutho.containsKey(permission)) {
                boolean currentValue = myAutho.get(permission);
                myAutho.put(permission, !currentValue);
            }
        }
    }

    public boolean hasAutho(Permission per) {
        return myAutho.get(per);
    }

    public Map<Permission, Boolean> getMyAutho() {
        return myAutho;
    }

    public void setMyAutho(Map<Permission, Boolean> myAutho) {
        this.myAutho = myAutho;
    }
}
