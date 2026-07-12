package ru.ifmo.person.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.ifmo.person.dto.ImportHistoryDto;
import ru.ifmo.person.dto.PersonDto;
import ru.ifmo.person.service.ImportService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Path("/import")
@Produces(MediaType.APPLICATION_JSON)
public class ImportController {

    @Inject
    private ImportService importService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @POST
    @Path("/persons")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response importPersons(
            @QueryParam("username") @DefaultValue("anonymous") String username,
            String jsonContent) {
        try {
            PersonDto[] personsArray = objectMapper.readValue(jsonContent, PersonDto[].class);
            List<PersonDto> persons = Arrays.asList(personsArray);

            if (persons.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Empty import data")).build();
            }

            ImportHistoryDto result = importService.importPersons(persons, username);

            if (result.getStatus().name().equals("SUCCESS")) {
                return Response.status(Response.Status.CREATED).entity(result).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
            }

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Failed to parse JSON: " + e.getMessage())).build();
        }
    }

    @GET
    @Path("/history")
    public Response getImportHistory(@QueryParam("username") String username) {
        try {
            List<ImportHistoryDto> history;
            if (username != null && !username.isEmpty()) {
                history = importService.getHistoryByUsername(username);
            } else {
                history = importService.getAllHistory();
            }
            return Response.ok(history).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/history/{id}")
    public Response getImportHistoryById(@PathParam("id") Long id) {
        try {
            ImportHistoryDto history = importService.getHistoryById(id);
            if (history == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Import history not found")).build();
            }
            return Response.ok(history).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }
}
