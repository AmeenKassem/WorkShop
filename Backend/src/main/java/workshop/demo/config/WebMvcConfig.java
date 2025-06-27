package workshop.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import workshop.demo.ApplicationLayer.AppSettingsService;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AppInitializationInterceptor appInitializationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(appInitializationInterceptor).addPathPatterns("/**");
    }

}
