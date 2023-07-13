package cz.cvut.fel.ear.meetingroomreservation.controller;

import cz.cvut.fel.ear.meetingroomreservation.dto.ReservationDTO;
import cz.cvut.fel.ear.meetingroomreservation.mapper.ReservationMapper;
import cz.cvut.fel.ear.meetingroomreservation.model.Customer;
import cz.cvut.fel.ear.meetingroomreservation.model.Reservation;
import cz.cvut.fel.ear.meetingroomreservation.security.SecurityUtils;
import cz.cvut.fel.ear.meetingroomreservation.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller class for managing reservations.
 * Handles RESTful API endpoints related to reservations.
 */
@RestController
@RequestMapping("rest/reservations")
public class ReservationController {

    private static final Logger LOG = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;
    private final ReservationMapper mapper;

    /**
     * Constructor for ReservationController class.
     *
     * @param reservationService The ReservationService used for managing reservations.
     * @param mapper             The ReservationMapper used for mapping Reservation entities to ReservationDTO objects.
     */
    @Autowired
    public ReservationController(ReservationService reservationService, ReservationMapper mapper) {
        this.reservationService = reservationService;
        this.mapper = mapper;
    }

    /**
     * Retrieves a list of all reservations.
     *
     * @return A List of ReservationDTO objects representing the reservations.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ReservationDTO> getReservations() {
        List<Reservation> reservations = reservationService.findAllReservations();
        return reservations.stream().map(mapper::entityToDTO).collect(Collectors.toList());
    }

    /**
     * Retrieves a reservation by its ID.
     *
     * @param id The ID of the reservation to retrieve.
     * @return A Reservation object representing the reservation.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Reservation getReservationById(@PathVariable Long id) {
        return reservationService.findReservationById(id);
    }

    /**
     * Sets the status of all expired reservations as finished.
     * Only accessible to users with the 'ROLE_ADMIN' role.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/setfinished")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setFinished() {
        reservationService.setFinishedReservationsAsFinished();
        LOG.debug("The status of all expired reservations is set to finished.");
    }

    /**
     * Deletes a reservation.
     * Accessible to users with the 'ROLE_WORKER' or 'ROLE_ADMIN' role.
     *
     * @param id The ID of the reservation to delete.
     */
    @PreAuthorize("hasAnyRole('ROLE_WORKER', 'ROLE_ADMIN')")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable Long id) {
        final Customer current = SecurityUtils.getCurrentCustomer();
        final Reservation reservation = getReservationById(id);
        reservationService.cancelReservation(current, reservation);
        LOG.debug("Reservation with id {} has been deleted", id);
    }
}
