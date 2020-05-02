package com.example.demo.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// this Class is called when the authorization is successful
// based on the users and their roles, we can direct to their webpages
@Slf4j
@Component
public class MySimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public static final Map<String, String> roleTargetUrlMap = new HashMap<>();

    static {
        roleTargetUrlMap.put("ROLE_STUDENT", "/studentsHomePage");
        roleTargetUrlMap.put("ROLE_ADMIN", "/courses");
        roleTargetUrlMap.put("ROLE_ADMIN_TRAINEE", "/courses");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        handleRequest(request, response, authentication);
        clearAuthenticationAttributes(request);
    }

    protected void handleRequest(HttpServletRequest request, HttpServletResponse response,
                                 Authentication authentication) throws IOException {

        SavedRequest savedRequest = this.requestCache.getRequest(request, response);
        String targetUrl = "/";
        if (savedRequest != null) {
            // if a particular URL(/courses) is called, then request goes on login page
            // after successful login, will return to the previously asked page(/courses)
            targetUrl = savedRequest.getRedirectUrl();

        } else {

            // if no specific page was called(means called /login), then after successful login
            // the default page will be called based on the user's ROLE
            targetUrl = this.determineTargetUrl(authentication);
            if (response.isCommitted()) {
                log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
                return;
            }
        }
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(final Authentication authentication) {

        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (final GrantedAuthority grantedAuthority : authorities) {
            String authorityName = grantedAuthority.getAuthority();
            if (roleTargetUrlMap.containsKey(authorityName)) {
                return roleTargetUrlMap.get(authorityName);
            }
        }

        return "/";
    }

    //  removing any remaining session data after finishing the handling of the request.
    protected void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}
