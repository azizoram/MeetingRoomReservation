package cz.cvut.fel.ear.meetingroomreservation.controller;

import cz.cvut.fel.ear.meetingroomreservation.dto.CustomerDTO;
import cz.cvut.fel.ear.meetingroomreservation.mapper.CustomerMapper;
import cz.cvut.fel.ear.meetingroomreservation.model.Admin;
import cz.cvut.fel.ear.meetingroomreservation.model.Customer;
import cz.cvut.fel.ear.meetingroomreservation.model.Worker;
import cz.cvut.fel.ear.meetingroomreservation.controller.util.RestUtil;
import cz.cvut.fel.ear.meetingroomreservation.security.model.AuthenticationToken;
import cz.cvut.fel.ear.meetingroomreservation.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller class for managing customers.
 * Handles RESTful API endpoints related to customers.
 */
@RestController
@RequestMapping("/rest/users")
public class CustomerController {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;
    private final CustomerMapper mapper;

    /**
     * Constructor for CustomerController class.
     *
     * @param customerService The CustomerService used for managing customers.
     * @param mapper          The CustomerMapper used for mapping Customer objects to CustomerDTO objects.
     */
    @Autowired
    public CustomerController(CustomerService customerService, CustomerMapper mapper) {
        this.customerService = customerService;
        this.mapper = mapper;
    }

    /**
     * Retrieves a list of all customers.
     *
     * @return A List of CustomerDTO objects representing the customers.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CustomerDTO> getCustomers() {
        List<Customer> customers = customerService.findAllCustomers();
        return customers.stream().map(mapper::customerToCustomerDTO).collect(Collectors.toList());
    }

    /**
     * Retrieves a customer by their ID.
     *
     * @param id The ID of the customer to retrieve.
     * @return A Customer object representing the customer.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Customer getCustomerById(@PathVariable Long id) {
        return customerService.findCustomerById(id);
    }

    /**
     * Adds a new admin user.
     * Only accessible to users with the 'ROLE_ADMIN' role.
     *
     * @param admin The Admin object to add.
     * @return A ResponseEntity with the HTTP headers and status code indicating the result of the operation.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/admin", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addNewAdmin(@RequestBody Admin admin) {
        customerService.persist(admin);
        LOG.debug("Added admin {} with id {}.", admin.getUsername(), admin.getUid());
        final HttpHeaders headers = RestUtil.createLocationHeaderFromCurrentUri("/{id}", admin.getUid());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    /**
     * Adds a new worker user.
     * Only accessible to users with the 'ROLE_ADMIN' role.
     *
     * @param worker The Worker object to add.
     * @return A ResponseEntity with the HTTP headers and status code indicating the result of the operation.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/worker", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addNewWorker(@RequestBody Worker worker) {
        customerService.persist(worker);
        LOG.debug("Added worker {} with id {}.", worker.getUsername(), worker.getUid());
        final HttpHeaders headers = RestUtil.createLocationHeaderFromCurrentUri("/{id}", worker.getUid());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    /**
     * Retrieves the currently authenticated customer.
     * Only accessible to users with the 'ROLE_ADMIN' or 'ROLE_WORKER' role.
     *
     * @param principal The Principal object representing the authenticated user.
     * @return A CustomerDTO object representing the currently authenticated customer.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_WORKER')")
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public CustomerDTO getCurrent(Principal principal) {
        final AuthenticationToken auth = (AuthenticationToken) principal;
        Customer customer = auth.getPrincipal().getCustomer();
        return mapper.customerToCustomerDTO(customer);
    }

    /**
     * Sets a worker as a priority worker.
     * Only accessible to users with the 'ROLE_ADMIN' role.
     *
     * @param id The ID of the worker to set as a priority.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}/prior")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPrior(@PathVariable Long id) {
        final Worker worker = (Worker) getCustomerById(id);
        customerService.setWorkerIsPrior(worker);
        LOG.debug("Worker with id {} is PRIOR", id);
    }

    /**
     * Sets a worker as a non-priority worker.
     * Only accessible to users with the 'ROLE_ADMIN' role.
     *
     * @param id The ID of the worker to set as a non-priority.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}/nonprior")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setNonPrior(@PathVariable Long id) {
        final Worker worker = (Worker) getCustomerById(id);
        customerService.setWorkerIsNotPrior(worker);
        LOG.debug("Worker with id {} is NONPRIOR", id);
    }
}
