package ru.ifmo.person.controller;

import ru.ifmo.person.dto.LocationDto;
import ru.ifmo.person.service.LocationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/locations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LocationController {
    @Inject
    private LocationService locationService;

    @GET
    public Response getAll() {
        return Response.ok(locationService.getAllLocations()).build();
    }

    @GET
    @Path("/distinct")
    public Response getDistinct() {
        return Response.ok(locationService.getDistinctLocations()).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Integer id) {
        LocationDto location = locationService.getLocationById(id);
        if (location == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Location not found")).build();
        }
        return Response.ok(location).build();
    }

    @POST
    public Response create(LocationDto location) {
        try {
            LocationDto created = locationService.createLocation(location);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }
}

