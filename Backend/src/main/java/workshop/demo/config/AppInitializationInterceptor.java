package workshop.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import workshop.demo.ApplicationLayer.AppSettingsService;

@Component
public class AppInitializationInterceptor implements HandlerInterceptor {

    @Autowired
    private AppSettingsService appSettingsService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        try {
            boolean isInitialized = appSettingsService.isInitialized();

            String requestURI = request.getRequestURI();
            System.out.println("[Interceptor] isInitialized=" + isInitialized + ", requestURI=" + requestURI);

            if (!isInitialized) {
                if (!requestURI.startsWith("/admin/init") && !requestURI.startsWith("/api/appsettings/admin/init")) {
                    response.sendRedirect("/404");
                    return false;
                }
            }
        } catch (Exception e) {
            // If checking initialization fails for any reason (e.g., DB error),
            // redirect to /404 instead of letting the exception propagate
            response.sendRedirect("/404");
            return false;
        }

        return true;
    }

}
