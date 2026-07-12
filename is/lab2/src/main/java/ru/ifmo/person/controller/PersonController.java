package ru.ifmo.person.controller;

import ru.ifmo.person.dto.PersonDto;
import ru.ifmo.person.enumeration.Color;
import ru.ifmo.person.enumeration.Country;
import ru.ifmo.person.service.PersonService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonController {

    @Inject
    private PersonService personService;

    @GET
    public Response getAll(@QueryParam("page") @DefaultValue("0") int page,
                          @QueryParam("size") @DefaultValue("10") int size,
                          @QueryParam("name") String name,
                          @QueryParam("nationality") String nationality,
                          @QueryParam("sortBy") String sortBy) {
        try {
            List<PersonDto> persons = personService.getPersonsFiltered(page, size, name, nationality, sortBy);
            long totalElements = personService.countFiltered(name, nationality);
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", persons);
            result.put("totalElements", totalElements);
            result.put("page", page);
            result.put("size", size);
            
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        PersonDto person = personService.getPersonById(id);
        if (person == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Person not found")).build();
        }
        return Response.ok(person).build();
    }

    @POST
    public Response create(@Valid PersonDto person) {
        try {
            PersonDto created = personService.createPerson(person);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (ConstraintViolationException e) {
            Map<String, String> errors = new HashMap<>();
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("errors", errors)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid PersonDto person) {
        try {
            person.setId(id);
            PersonDto updated = personService.updatePerson(person);
            return Response.ok(updated).build();
        } catch (ConstraintViolationException e) {
            Map<String, String> errors = new HashMap<>();
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("errors", errors)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.WILDCARD)
    public Response delete(@PathParam("id") Long id) {
        try {
            personService.deletePerson(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            if ("Person not found".equals(e.getMessage())) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/by-nationality")
    @Consumes(MediaType.WILDCARD)
    public Response deleteByNationality(@QueryParam("nationality") String nationality) {
        try {
            if (nationality == null || nationality.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Nationality parameter is required")).build();
            }
            personService.deleteByNationality(Country.valueOf(nationality));
            return Response.ok(Map.of("message", "Deleted successfully")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid nationality value")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/statistics/average-height")
    public Response getAverageHeight() {
        try {
            Double avg = personService.getAverageHeight();
            return Response.ok(Map.of("averageHeight", avg)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/nationalities")
    public Response getUniqueNationalities() {
        try {
            return Response.ok(personService.getUniqueNationalities()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/statistics/hair-color-percentage")
    public Response getHairColorPercentage(@QueryParam("color") String color) {
        try {
            if (color == null || color.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Color parameter is required")).build();
            }
            Double percentage = personService.getHairColorPercentage(Color.valueOf(color));
            return Response.ok(Map.of("percentage", percentage)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid color value")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/count")
    public Response countByHairColorAndLocation(@QueryParam("hairColor") String hairColor,
                                               @QueryParam("locationId") Integer locationId) {
        try {
            if (hairColor == null || hairColor.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Hair color parameter is required")).build();
            }
            Long count = personService.countByHairColorAndLocation(Color.valueOf(hairColor), locationId);
            return Response.ok(Map.of("count", count)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid hair color value")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }
}
