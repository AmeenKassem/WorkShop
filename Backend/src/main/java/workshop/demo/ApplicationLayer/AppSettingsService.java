package workshop.demo.ApplicationLayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DomainLayer.AppSettings.AppSettingsEntity;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.InfrastructureLayer.AppSettingsRepository;

@Service
public class AppSettingsService {

    @Autowired
    private AppSettingsRepository appSettingsRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private DatabaseCleaner dataBase;
    @Autowired
    private AdminInitilizer adminInitilizer;

    public boolean isInitialized() {
        // try{
        AppSettingsEntity settings = appSettingsRepository.findById(1L)
                .orElseGet(() -> {
                    AppSettingsEntity newSettings = new AppSettingsEntity();
                    appSettingsRepository.save(newSettings);
                    return newSettings;
                });
        return settings.isInitialized();
        // }catch(Exception e){
        // System.out.println(e.getMessage());
        // return false;
        // }
    }

    public void markInitialized(String userName, String password, String key) throws Exception {

        // userService.registerAdmin(userName, password, key);
        try {
            userService.registerAdmin(userName, password, key);
            System.out.println("[Admin Init] Admin registered successfully.");
        } catch (UIException ex) {
            if (ex.getNumber() == ErrorCodes.USERNAME_USED) {
                // admin already exists → log and continue
                System.out.println("[Admin Init] Admin already exists. Continuing startup.");
            } else {
                // rethrow to fail initialization
                throw ex;
            }
        }

        AppSettingsEntity settings = appSettingsRepository.findById(1L)
                .orElse(new AppSettingsEntity());
        settings.setInitialized(true);
        appSettingsRepository.save(settings);
    }

    public void markShutdown(String key) throws UIException {
        if (!"123321".equals(key)) {
            throw new UIException("NOT THE ADMIN", 1039);
        }
        AppSettingsEntity settings = appSettingsRepository.findById(1L)
                .orElse(new AppSettingsEntity());
        settings.setInitialized(false);
        appSettingsRepository.save(settings);
    }

    public void deleteData(String adminKey, String username, String password) throws UIException {
        userService.checkAdmin(adminKey, username, password);
        dataBase.wipeDatabase();
        markShutdown(adminKey);
    }

}
