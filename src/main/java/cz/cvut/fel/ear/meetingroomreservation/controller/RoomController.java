package cz.cvut.fel.ear.meetingroomreservation.controller;

import cz.cvut.fel.ear.meetingroomreservation.dto.RoomDTO;
import cz.cvut.fel.ear.meetingroomreservation.exception.NotFoundException;
import cz.cvut.fel.ear.meetingroomreservation.exception.ValidationException;
import cz.cvut.fel.ear.meetingroomreservation.mapper.RoomMapper;
import cz.cvut.fel.ear.meetingroomreservation.model.*;
import cz.cvut.fel.ear.meetingroomreservation.controller.util.RestUtil;
import cz.cvut.fel.ear.meetingroomreservation.security.SecurityUtils;
import cz.cvut.fel.ear.meetingroomreservation.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller class for managing rooms.
 * Handles RESTful API endpoints related to rooms.
 */
@RestController
@RequestMapping("rest/rooms")
public class RoomController {

    private static final Logger LOG = LoggerFactory.getLogger(RoomController.class);

    private final RoomService roomService;
    private final RoomMapper mapper;

    /**
     * Constructor for RoomController class.
     *
     * @param roomService The RoomService used for managing rooms.
     * @param mapper      The RoomMapper used for mapping Room entities to RoomDTO objects.
     */
    @Autowired
    public RoomController(RoomService roomService, RoomMapper mapper) {
        this.roomService = roomService;
        this.mapper = mapper;
    }

    /**
     * Retrieves a list of all rooms.
     *
     * @return A List of RoomDTO objects representing the rooms.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RoomDTO> getRooms() {
        List<Room> rooms = roomService.findAllRooms();
        return rooms.stream().map(mapper::entityToDTO).collect(Collectors.toList());
    }

    /**
     * Retrieves a room by its ID.
     *
     * @param id The ID of the room to retrieve.
     * @return A Room object representing the room.
     * @throws NotFoundException if the room with the specified ID is not found.
     */
    @GetMapping(value =  "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Room getRoomById(@PathVariable Long id) {
        final Room room = roomService.findRoomById(id);
        if (room == null) {
            throw NotFoundException.create("Room", id);
        }
        return room;
    }

    /**
     * Retrieves a list of room alterations for a room.
     * Accessible to users with the 'ROLE_WORKER' or 'ROLE_ADMIN' role.
     *
     * @param id The ID of the room to retrieve the alterations for.
     * @return A List of RoomAlteration objects representing the room alterations.
     */
    @PreAuthorize("hasAnyRole('ROLE_WORKER', 'ROLE_ADMIN')")
    @GetMapping(value = "/{id}/alters", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RoomAlteration> getAlters(@PathVariable Long id) {
        return getRoomById(id).getRoomAlterations();
    }

    /**
     * Adds a new room.
     * Only accessible to users with the 'ROLE_ADMIN' role.
     *
     * @param room The Room object to add.
     * @return A ResponseEntity with the HTTP headers and status code indicating the result of the operation.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addNewRoom(@RequestBody Room room) {
        roomService.persist(room);
        LOG.debug("Added room {}.", room.getRid());
        final HttpHeaders headers = RestUtil.createLocationHeaderFromCurrentUri("/{id}", room.getRid());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    /**
     * Updates an existing room.
     * Only accessible to users with the 'ROLE_ADMIN' role.
     *
     * @param id   The ID of the room to update.
     * @param room The updated Room object.
     * @throws NotFoundException    if the room with the specified ID is not found.
     * @throws ValidationException  if the ID in the request data does not match the ID in the request URL.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRoom(@PathVariable Long id, @RequestBody Room room) {
        final Room original = roomService.findRoomById(id);
        if (original == null) {
            throw NotFoundException.create("Room", id);
        }
        if (!original.getRid().equals(room.getRid())) {
            throw new ValidationException("Room id in the data does not match the one in the request URL");
        }
        roomService.update(room);
        LOG.debug("Updated room {}.", room.getRid());
    }

    /**
     * Creates a new reservation on a non-priority room.
     * Accessible to users with the 'ROLE_WORKER' role.
     *
     * @param id          The ID of the room to create the reservation on.
     * @param reservation The Reservation object to create.
     */
    @PreAuthorize("hasRole('ROLE_WORKER')")
    @PostMapping(value = "/{id}/newnon", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void createNewReservationOnNon(@PathVariable Long id, @RequestBody Reservation reservation) {
        final Room room = getRoomById(id);
        final Customer current = SecurityUtils.getCurrentCustomer();
        reservation.setCustomer(current);
        reservation.setRoom(room);
        reservation.setStatus(ReservationStatus.ACTIVE);
        roomService.createReservationOnNonPriorityRoom(room, reservation);
        LOG.debug("Reservation on NONPRIOR room has been added");
    }

    /**
     * Creates a new reservation on a priority room.
     * Accessible to users with the 'ROLE_WORKER' role.
     *
     * @param id          The ID of the room to create the reservation on.
     * @param reservation The Reservation object to create.
     */
    @PreAuthorize("hasRole('ROLE_WORKER')")
    @PostMapping(value = "/{id}/newprior", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void createNewReservationOnPrior(@PathVariable Long id, @RequestBody Reservation reservation) {
        final Room room = getRoomById(id);
        final Customer current = SecurityUtils.getCurrentCustomer();
        reservation.setCustomer(current);
        reservation.setRoom(room);
        reservation.setStatus(ReservationStatus.ACTIVE);
        roomService.createReservationOnPriorityRoom(room, reservation);
        LOG.debug("Reservation on PRIOR room has been added");
    }

    /**
     * Creates a new weekly reservation for a room.
     * Accessible to users with the 'ROLE_WORKER' role.
     *
     * @param id The ID of the room to create the weekly reservation for.
     */
    @PreAuthorize("hasRole('ROLE_WORKER')")
    @PostMapping(value = "/{id}/weekly", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void createWeeklyReservation(@PathVariable Long id) {
        final Room room = getRoomById(id);
        final Customer current = SecurityUtils.getCurrentCustomer();
        roomService.createWeeklyReservation(room, current);
        LOG.debug("Added weekly reservation");
    }

    /**
     * Sets a room as a priority room.
     * Only accessible to users with the 'ROLE_ADMIN' role.
     *
     * @param id The ID of the room to set as priority.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}/prior")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPrior(@PathVariable Long id) {
        final Room room = getRoomById(id);
        roomService.setRoomIsPrior(room);
        LOG.debug("Room {} is PRIOR.", id);
    }

    /**
     * Sets a room as a non-priority room.
     * Only accessible to users with the 'ROLE_ADMIN' role.
     *
     * @param id The ID of the room to set as non-priority.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}/nonprior")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setNonPrior(@PathVariable Long id) {
        final Room room = getRoomById(id);
        roomService.setRoomIsNotPrior(room);
        LOG.debug("Room {} is NONPRIOR.", id);
    }

    /**
     * Adds equipment to a room.
     * Only accessible to users with the 'ROLE_ADMIN' role.
     *
     * @param id        The ID of the room to add the equipment to.
     * @param equipment The Equipment object to add.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/{id}/equipment", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void addEquip(@PathVariable Long id, @RequestBody Equipment equipment) {
        final Admin current = (Admin) SecurityUtils.getCurrentCustomer();
        final Room room = getRoomById(id);
        equipment.setRoom(room);
        roomService.addEquipment(current, room, equipment);
        LOG.debug("Added equipment {}.", id);
    }
}
