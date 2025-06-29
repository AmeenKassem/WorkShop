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
    @Autowired
    private DatabaseCleaner dataBase;

    public boolean isInitialized() {
        AppSettingsEntity settings = appSettingsRepository.findById(1L)
                .orElseGet(() -> {
                    AppSettingsEntity newSettings = new AppSettingsEntity();
                    appSettingsRepository.save(newSettings);
                    return newSettings;
                });
        return settings.isInitialized();
    }

    public void markInitialized(String userName, String password, String key) throws Exception {

        userService.registerAdmin(userName, password, key);
        AppSettingsEntity settings = appSettingsRepository.findById(1L)
                .orElse(new AppSettingsEntity());
        settings.setInitialized(true);
        appSettingsRepository.save(settings);
    }

 

    public void markShutdown(String key) throws UIException {
        if (key != "123321") {
            throw new UIException("NOT THE ADMIN", 1039);
        }
        AppSettingsEntity settings = appSettingsRepository.findById(1L)
                .orElse(new AppSettingsEntity());
        settings.setInitialized(false);
        appSettingsRepository.save(settings);
    }



    public void deleteData(String adminKey,String username,String password) throws UIException{
        userService.checkAdmin( adminKey, username,password);
        dataBase.wipeDatabase();
        markShutdown(adminKey);
    }


}
