package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "[authorization]")
public class Authorization {

    private static final Logger logger = LoggerFactory.getLogger(Authorization.class);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "authorization_permissions", joinColumns = @JoinColumn(name = "auth_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "is_authorized")
    private Map<Permission, Boolean> myAutho = new HashMap<>();

    ;

    public Authorization() {
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
