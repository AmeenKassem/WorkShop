package workshop.demo.DomainLayer.AppSettings;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class AppSettingsEntity {

    @Id
    private Long id = 1L;

    private boolean isInitialized;

    public AppSettingsEntity() {
    }

    public AppSettingsEntity(boolean isInitialized) {
        this.id = 1L;
        this.isInitialized = isInitialized;
    }

    public Long getId() {
        return id;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }
}
