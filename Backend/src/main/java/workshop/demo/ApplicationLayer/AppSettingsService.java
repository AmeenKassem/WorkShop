package workshop.demo.ApplicationLayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DomainLayer.AppSettings.AppSettingsEntity;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.InfrastructureLayer.AppSettingsRepository;

@Service
public class AppSettingsService {

    @Autowired
    private AppSettingsRepository appSettingsRepository;
    @Autowired
    private UserService userService;

    public boolean isInitialized() {
        AppSettingsEntity settings = appSettingsRepository.findById(1L)
                .orElseGet(() -> {
                    AppSettingsEntity newSettings = new AppSettingsEntity();
                    appSettingsRepository.save(newSettings);
                    return newSettings;
                });
        return settings.isInitialized();
    }

    public void markInitialized(String userName, String password, int key) throws UIException {
        if (!userService.isAdmin(userName, password) || key != 123321) {
            throw new UIException("NOT THE ADMIN", 1039);
        }
        AppSettingsEntity settings = appSettingsRepository.findById(1L)
                .orElse(new AppSettingsEntity());
        settings.setInitialized(true);
        appSettingsRepository.save(settings);
    }
}
