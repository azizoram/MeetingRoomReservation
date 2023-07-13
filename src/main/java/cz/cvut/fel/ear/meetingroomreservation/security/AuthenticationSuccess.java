package cz.cvut.fel.ear.meetingroomreservation.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.ear.meetingroomreservation.security.model.CustomerDetails;
import cz.cvut.fel.ear.meetingroomreservation.security.model.LoginStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Service class for handling successful authentication and logout.
 * Implements the AuthenticationSuccessHandler and LogoutSuccessHandler interfaces.
 */
@Service
public class AuthenticationSuccess implements AuthenticationSuccessHandler, LogoutSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationSuccess.class);

    private final ObjectMapper mapper;

    /**
     * Retrieves the username from the authenticated user.
     *
     * @param authentication The Authentication object containing the user's authentication information.
     * @return The username of the authenticated user.
     */
    private String getUsername(Authentication authentication){
        if (authentication == null){
            return "";
        }
        return ((CustomerDetails) authentication.getPrincipal()).getUsername();
    }

    /**
     * Constructor for AuthenticationSuccess class.
     *
     * @param mapper The ObjectMapper used for JSON serialization.
     */
    @Autowired
    public AuthenticationSuccess(ObjectMapper mapper){
        this.mapper = mapper;
    }

    /**
     * Handles successful authentication.
     *
     * @param request        The HttpServletRequest object.
     * @param response       The HttpServletResponse object.
     * @param authentication The Authentication object containing the user's authentication information.
     * @throws IOException if an I/O error occurs during response writing.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException{
        final String username = getUsername(authentication);

        LOGGER.trace("Successfully authenticated user {} .", username);

        final LoginStatus loginStatus = new LoginStatus(true,  username, null, authentication.isAuthenticated());
        mapper.writeValue(response.getOutputStream(), loginStatus);
    }

    /**
     * Handles successful logout.
     *
     * @param request        The HttpServletRequest object.
     * @param response       The HttpServletResponse object.
     * @param authentication The Authentication object containing the user's authentication information.
     * @throws IOException if an I/O error occurs during response writing.
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException{
        LOGGER.trace("Successfully logged out user {} .", getUsername(authentication));

        final LoginStatus loginStatus = new LoginStatus(false, null, null, true);

        mapper.writeValue(response.getOutputStream(), loginStatus);
    }

}
