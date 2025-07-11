package workshop.demo.Controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.AppSettingsService;
import workshop.demo.DataInitilizer.InitDataService;
import workshop.demo.DomainLayer.Exceptions.UIException;

@RestController
@RequestMapping("/api/appsettings")
public class AppSettingsController {

    @Autowired
    private AppSettingsService appSettingsService;
    // private final AdminHandler adminHandler;

    @Autowired
    private InitDataService dataInit;

    // ---------system init:
    @PostMapping("admin/init")
    public ResponseEntity<?> initializeSite(@RequestParam String key,
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
    public ResponseEntity<ApiResponse<Boolean>> isSiteInitialized() {
        System.out.println("hiiiiiiiiiiiiii");
        boolean initialized = appSettingsService.isInitialized();
        System.out.println("hiiiiiiiiiiiiii");
        return ResponseEntity.ok(new ApiResponse<>(initialized, null));
    }

    @PostMapping("/shutdown")
    public ResponseEntity<?> shutdownSystem(@RequestParam String key) {
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

    @PostMapping("/initdatafile")
    public ResponseEntity<?> init(@RequestParam String key,
            @RequestParam String userName,
            @RequestParam String password) {
        try {
            // appSettingsService.markShutdown(key);
            String res = dataInit.init(key, userName, password);
            return ResponseEntity.ok(new ApiResponse<>(res, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "something wrong with reading file:\n" + e.getMessage(), -1));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "something wrong with reading file:\n" + e.getMessage(), -1));
        }
    }

    @PostMapping("/resetdata")
    public ResponseEntity<?> reset(@RequestParam String key,
            @RequestParam String userName,
            @RequestParam String password) {
        try {
            System.out.println("hiiiii");
            appSettingsService.deleteData(key, userName, password);
            System.out.println("hiiiii");
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
