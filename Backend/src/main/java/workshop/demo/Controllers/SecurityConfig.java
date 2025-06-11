package workshop.demo.Controllers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector)
            throws Exception {
        // MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);

        //  http
        //     // 1. Explicitly disable CSRF for the entire application (as per your current code)
        //     .csrf(csrf -> csrf.disable())

        //     // 2. Configure headers, specifically to allow H2 console to load within a frame
        //     .headers(headers -> headers
        //         .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Allow frames from the same origin
        //         // If .sameOrigin() still causes issues, you can try .disable()
        //         // .frameOptions(frameOptions -> frameOptions.disable()) // Less secure, use only if necessary for dev
        //     )

        //     // 3. Authorize HTTP requests
        //     .authorizeHttpRequests(auth -> auth
        //         // Allow public access to the H2 console path
        //         .requestMatchers(mvcMatcherBuilder.pattern("/h2-console/**")).permitAll()
        //         // You can define other rules here, e.g., securing other endpoints
        //         // For example, if you want all other requests to be authenticated:
        //         .anyRequest().authenticated()
        //         // Or if you want all other requests also permitted (not common for real apps):
        //         // .anyRequest().permitAll()
        //     );

        http
                .csrf().disable()
                .authorizeHttpRequests()
                .anyRequest().permitAll();
        // MvcRequestMatcher.Builder mvcMatcherBuilder = new
        // MvcRequestMatcher.Builder(null);

        // http
        // .csrf(csrf -> csrf
        // // CSRF is still handled for the H2 console to allow it to function
        // .ignoringRequestMatchers(mvcMatcherBuilder.pattern("/h2-console/**")))
        // .headers(headers -> headers
        // // Frame options are still set for the H2 console
        // .frameOptions(frameOptions -> frameOptions.sameOrigin()))
        // // No explicit authorization rules for /h2-console here,
        // // as it will be ignored by webSecurityCustomizer below
        // .authorizeHttpRequests(auth -> auth
        // // Example: require authentication for all other requests
        // .anyRequest().authenticated());
        return http.build();
    }

    // @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
                .and().csrf().disable()
                .headers().frameOptions().disable(); // Allow H2 console in frames
    }
}
