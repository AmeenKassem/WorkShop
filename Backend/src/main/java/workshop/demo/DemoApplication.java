package workshop.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.DomainLayer.AppSettings.AppSettingsEntity;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.InfrastructureLayer.AppSettingsRepository;

@SpringBootApplication
@EnableCaching
public class DemoApplication {

    @Autowired
    private UserService userService;
    @Autowired
    private AppSettingsRepository appsetting;
    @Autowired
    private AdminInitilizer adminInitilizer;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(UserService userService) {
        return args -> {
            try {
                //userService.registerAdminDirectly("admin", "Admin123", 23);
                userService.registerAdmin("admin", "Admin123", adminInitilizer.getPassword());
                System.out.println("[Admin Init] Admin registered successfully.");
            } catch (UIException ex) {
                // Admin already exists — just log and continue -> the system does not crash
                System.out.println("[Admin Init] Admin already exists. Continuing startup.");
            } catch (Exception e) {
                System.err.println("[Admin Init] Unexpected error: " + e.getMessage());
            }
        };
    }

    @Bean
    CommandLineRunner initAppSettings(AppSettingsRepository appSettingsRepository) {
        return args -> {
            if (!appSettingsRepository.existsById(1L)) {
                AppSettingsEntity appSettings = new AppSettingsEntity(false);
                appSettingsRepository.save(appSettings);
                System.out.println("[App Settings Init] Created AppSettingsEntity.");
            } else {
                System.out.println("[App Settings Init] AppSettingsEntity already exists.");
            }
        };
    }

}
