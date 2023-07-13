package cz.cvut.fel.ear.meetingroomreservation.service.security;

import cz.cvut.fel.ear.meetingroomreservation.repository.CustomerDao;
import cz.cvut.fel.ear.meetingroomreservation.model.Customer;
import cz.cvut.fel.ear.meetingroomreservation.security.model.CustomerDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * Service class for loading customer details for authentication.
 * Implements the UserDetailsService interface.
 */
@Service
public class CustomerDetailsService implements UserDetailsService {

    private final CustomerDao customerDao;


    /**
     * Constructor for CustomerDetailsService class.
     *
     * @param customerDao The CustomerDao used for retrieving customer information.
     */
    @Autowired
    public CustomerDetailsService(CustomerDao customerDao){
        this.customerDao = customerDao;
    }

    /**
     * Loads user details by username.
     *
     * @param username The username of the customer.
     * @return A UserDetails object representing the customer details.
     * @throws UsernameNotFoundException if the customer with the specified username is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Customer customer = customerDao.findByUsername(username);

        if (customer == null){
            throw new UsernameNotFoundException("User with name {"+ username + "} was not found!");
        }
        return new CustomerDetails(customer);
    }
}
