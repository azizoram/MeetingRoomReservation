package cz.cvut.fel.ear.meetingroomreservation.security;

import cz.cvut.fel.ear.meetingroomreservation.model.Customer;
import cz.cvut.fel.ear.meetingroomreservation.security.model.AuthenticationToken;
import cz.cvut.fel.ear.meetingroomreservation.security.model.CustomerDetails;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Objects;

/**
 * Utility class for handling security-related operations.
 */
public class SecurityUtils {

    /**
     * Retrieves the currently authenticated customer.
     *
     * @return The currently authenticated customer.
     * @throws NullPointerException if the security context or customer details are null.
     */
    public static Customer getCurrentCustomer(){
        final SecurityContext context = SecurityContextHolder.getContext();
        Objects.requireNonNull(context);
        final CustomerDetails customerDetails = (CustomerDetails) context.getAuthentication().getDetails();
        return customerDetails.getCustomer();
    }

    /**
     * Retrieves the customer details of the currently authenticated customer.
     *
     * @return The customer details of the currently authenticated customer, or null if not available.
     */
    public static CustomerDetails getCustomerDetails(){
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null && context.getAuthentication().getDetails() instanceof CustomerDetails){
            return (CustomerDetails) context.getAuthentication().getDetails();
        }
        return null;
    }

    /**
     * Sets the current customer and authentication token in the security context.
     *
     * @param customerDetails The customer details to set as the current customer.
     * @return The created authentication token.
     */
    public static AuthenticationToken setCurrentCustomer(CustomerDetails customerDetails){
        final AuthenticationToken token = new AuthenticationToken(customerDetails.getAuthorities(), customerDetails);
        token.setAuthenticated(true);

        final SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
        return token;
    }


    /**
     * Checks if the current authentication is anonymous (not associated with a customer).
     *
     * @return true if the current authentication is anonymous, false otherwise.
     */
    public static boolean isAuthenticatedAnonymously(){
        return getCustomerDetails() == null;
    }
}
