package com.example.demo.configuration;

import com.example.demo.auth.ApplicationUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.concurrent.TimeUnit;

import static com.example.demo.configuration.UserPermission.*;
import static com.example.demo.configuration.UserRole.*;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // to be used for controller method level authorization using @PreAuthorize
@AllArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;

    private final ApplicationUserDetailsService applicationUserDetailsService;

    private final MySimpleUrlAuthenticationSuccessHandler authenticationSuccessHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                // Cross site Request Forgery - Recommended to disable for non-browser clients like postman, Curl
                                // and should be enabled for users using browsers to access our application
                // if we enable csrf, we need to send the csrf token for communication
                .authorizeRequests()

                // giving access to all the public pages
                .antMatchers("/","index","css/*","js/*").permitAll()

                //Giving access to students API to only Student role
                .antMatchers("/api/**").hasRole(STUDENT.name())

                .antMatchers(HttpMethod.POST,"/management/api/**").hasAuthority(STUDENT_WRITE.name())
                .antMatchers(HttpMethod.PUT,"/management/api/**").hasAuthority(STUDENT_WRITE.name())
                .antMatchers(HttpMethod.DELETE,"/management/api/**").hasAuthority(STUDENT_WRITE.name())
                .antMatchers(HttpMethod.GET,"/management/api/**").hasAnyRole(ADMIN.name(), ADMIN_TRAINEE.name())

                .anyRequest()
                .authenticated()
                .and()

                // used for non-browser clients like postman
                .httpBasic()      // Commenting the basic http security

                .and()

                // used for browser clients
                .formLogin()  // Enabling Form based login using spring security
                    .loginPage("/login")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .permitAll()
                    .successHandler(authenticationSuccessHandler)  // lands on the specific page based on the role of the user(coded for this)
//                    .successHandler(new SavedRequestAwareAuthenticationSuccessHandler())  // lands on the previously asked page after successful login, else home page("/")
//                    .defaultSuccessUrl("/courses", true)  // default url, always lands on the this page for all the users

                .and()

                .rememberMe()   // remembering the user login for 30 days, default is 2 weeks
                    .tokenValiditySeconds((int)TimeUnit.DAYS.toSeconds(30))
                    .key("someSecuredKey")
                    .rememberMeParameter("remember-me")
                .and()
                .logout()       // handling logout with manual steps to follow
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                    .clearAuthentication(true)
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID", "remember-me")
                    .logoutSuccessUrl("/login");

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(applicationUserDetailsService);
        return daoAuthenticationProvider;
    }

    // in- memory users details
    /*
    @Override
    @Bean
    protected UserDetailsService userDetailsService() {

        //creating in-memory user
        UserDetails inMemoryUserStudent = User.builder().username("dhana")
                .password(passwordEncoder.encode("password"))
//                .roles(STUDENT.name())  // ROLE_STUDENT
                .authorities(STUDENT.getGrantedAuthorities())
                .build();

        UserDetails inMemoryUserAdmin = User.builder().username("dhana1310")
                .password(passwordEncoder.encode("password123"))
//                .roles(ADMIN.name())  // ROLE_ADMIN
                .authorities(ADMIN.getGrantedAuthorities())
                .build();

        UserDetails inMemoryUserAdminTrainee = User.builder().username("mandeep")
                .password(passwordEncoder.encode("karamjeet"))
//                .roles(ADMIN_TRAINEE.name())  // ROLE_ADMIN_TRAINEE
                .authorities(ADMIN_TRAINEE.getGrantedAuthorities())
                .build();

        return new InMemoryUserDetailsManager(inMemoryUserStudent, inMemoryUserAdmin, inMemoryUserAdminTrainee);
    }
    */
}
