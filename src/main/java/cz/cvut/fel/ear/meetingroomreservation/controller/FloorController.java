package cz.cvut.fel.ear.meetingroomreservation.controller;

import cz.cvut.fel.ear.meetingroomreservation.dto.FloorDTO;
import cz.cvut.fel.ear.meetingroomreservation.exception.NotFoundException;
import cz.cvut.fel.ear.meetingroomreservation.exception.ValidationException;
import cz.cvut.fel.ear.meetingroomreservation.mapper.FloorMapper;
import cz.cvut.fel.ear.meetingroomreservation.model.Floor;
import cz.cvut.fel.ear.meetingroomreservation.controller.util.RestUtil;
import cz.cvut.fel.ear.meetingroomreservation.service.FloorService;
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
 * Controller class for managing floors.
 * Handles RESTful API endpoints related to floors.
 */
@RestController
@RequestMapping("rest/floors")
public class FloorController {

    private static final Logger LOG = LoggerFactory.getLogger(FloorController.class);

    private final FloorService floorService;
    private final FloorMapper mapper;

    /**
     * Constructor for FloorController class.
     *
     * @param floorService The FloorService used for managing floors.
     * @param mapper       The FloorMapper used for mapping Floor entities to FloorDTO objects.
     */
    @Autowired
    public FloorController(FloorService floorService, FloorMapper mapper) {
        this.floorService = floorService;
        this.mapper = mapper;
    }

    /**
     * Retrieves a list of all floors.
     *
     * @return A List of FloorDTO objects representing the floors.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FloorDTO> getFloors() {
        List<Floor> floors = floorService.findAllFloors();
        return floors.stream().map(mapper::entityToDTO).collect(Collectors.toList());
    }

    /**
     * Retrieves a floor by its ID.
     *
     * @param id The ID of the floor to retrieve.
     * @return A FloorDTO object representing the floor.
     * @throws NotFoundException if the floor with the specified ID is not found.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public FloorDTO getFloorById(@PathVariable Long id) {
        final Floor floor = floorService.findFloorById(id);
        if (floor == null) {
            throw NotFoundException.create("Floor", id);
        }
        return mapper.entityToDTO(floor);
    }

    /**
     * Adds a new floor.
     * Only accessible to users with the 'ROLE_ADMIN' role.
     *
     * @param floor The Floor object to add.
     * @return A ResponseEntity with the HTTP headers and status code indicating the result of the operation.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addNewFloor(@RequestBody Floor floor) {
        floorService.persist(floor);
        LOG.debug("Added floor {}.", floor.getFid());
        final HttpHeaders headers = RestUtil.createLocationHeaderFromCurrentUri("/{id}", floor.getFid());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }


    /**
     * Updates an existing floor.
     * Only accessible to users with the 'ROLE_ADMIN' role.
     *
     * @param id    The ID of the floor to update.
     * @param floor The updated Floor object.
     * @throws NotFoundException    if the floor with the specified ID is not found.
     * @throws ValidationException  if the ID in the request data does not match the ID in the request URL.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateFloor(@PathVariable Long id, @RequestBody Floor floor) {
        final Floor original = floorService.findFloorById(id);
        if (original == null) {
            throw NotFoundException.create("Floor", id);
        }
        if (!original.getFid().equals(floor.getFid())) {
            throw new ValidationException("Floor id in the data does not match the one in the request URL");
        }
        floorService.update(floor);
        LOG.debug("Updated floor {}.", floor.getFid());
    }
}
