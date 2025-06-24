package workshop.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.AppSettingsService;
import workshop.demo.DomainLayer.Exceptions.UIException;

@RestController
@RequestMapping("/api/appsettings")
public class AppSettingsController {

    @Autowired
    private AppSettingsService appSettingsService;
    // private final AdminHandler adminHandler;

    //---------system init:
    @PostMapping("admin/init")
    public ResponseEntity<?> initializeSite(@RequestParam int key,
            @RequestParam String userName,
            @RequestParam String password) {
        try {
            appSettingsService.markInitialized(userName, password, key);
            return ResponseEntity.ok(new ApiResponse<>(true, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/isInitialized")
    public ResponseEntity<?> isSiteInitialized() {
        boolean initialized = appSettingsService.isInitialized();
        return ResponseEntity.ok(new ApiResponse<>(initialized, null));
    }

    @PostMapping("/shutdown")
    public ResponseEntity<?> shutdownSystem(@RequestParam int key) {
        try {
            appSettingsService.markShutdown(key);
            return ResponseEntity.ok(new ApiResponse<>(true, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

}
