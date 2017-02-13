package lemming.auth;

import lemming.user.User;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

/**
 * A request filter for authentication with the wicket session.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthRequestFilter implements ContainerRequestFilter {
    @Context
    HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        HttpSession session = request.getSession(true);
        WebSession webSession = (WebSession) session.getAttribute("wicket:WicketFilter:session");

        if (webSession instanceof WebSession) {
            webSession.bind();
        }

        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public String getAuthenticationScheme() {
                return null;
            }

            @Override
            public Principal getUserPrincipal() {
                if (webSession instanceof WebSession) {
                    if (webSession.getUser() instanceof User) {
                        return webSession.getUser();
                    }
                }

                return null;
            }

            @Override
            public boolean isSecure() {
                return true;
            }

            @Override
            public boolean isUserInRole(String role) {
                if (webSession instanceof WebSession) {
                    if (webSession.getUser() instanceof User) {
                        return webSession.getUser().getRole().name().equals(role);
                    }
                }

                return false;
            }
        });
    }
}