package com.example.demo.configuration;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static com.example.demo.configuration.UserPermission.*;
import static com.example.demo.configuration.UserRole.*;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
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
                .httpBasic();
    }

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
}
