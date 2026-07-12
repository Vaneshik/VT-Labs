package ru.ifmo.person.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import ru.ifmo.person.dto.ImportHistoryDto;
import ru.ifmo.person.dto.PersonDto;
import ru.ifmo.person.service.ImportService;
import ru.ifmo.person.service.MinioService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RequestScoped
@Path("/import")
@Produces(MediaType.APPLICATION_JSON)
public class ImportController {

    @Inject
    private ImportService importService;

    @Inject
    private MinioService minioService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @POST
    @Path("/persons")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response importPersons(
            @MultipartForm MultipartFormDataInput input,
            @QueryParam("username") @DefaultValue("anonymous") String username) {
        try {
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
            List<InputPart> inputParts = uploadForm.get("file");
            
            if (inputParts == null || inputParts.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "No file provided")).build();
            }
            
            InputPart inputPart = inputParts.get(0);
            InputStream fileInputStream = inputPart.getBody(InputStream.class, null);
            
            // Get filename from Content-Disposition header
            String fileName = "import.json";
            String[] contentDisposition = inputPart.getHeaders().getFirst("Content-Disposition").split(";");
            for (String part : contentDisposition) {
                if (part.trim().startsWith("filename")) {
                    fileName = part.split("=")[1].trim().replaceAll("\"", "");
                }
            }
            
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = fileInputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] fileContent = buffer.toByteArray();
            
            String jsonContent = new String(fileContent);
            PersonDto[] personsArray = objectMapper.readValue(jsonContent, PersonDto[].class);
            List<PersonDto> persons = Arrays.asList(personsArray);

            if (persons.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Empty import data")).build();
            }

            ImportHistoryDto result = importService.importPersons(persons, username, fileContent, fileName);

            if (result.getStatus().name().equals("SUCCESS")) {
                return Response.status(Response.Status.CREATED).entity(result).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
            }

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Failed to process file: " + e.getMessage())).build();
        }
    }

    @GET
    @Path("/download/{historyId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadImportFile(@PathParam("historyId") Long historyId) {
        try {
            ImportHistoryDto history = importService.getHistoryById(historyId);
            if (history == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Import history not found")).build();
            }
            
            if (history.getFileName() == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "No file associated with this import")).build();
            }
            
            InputStream fileStream = minioService.downloadFile(history.getFileName());
            
            StreamingOutput stream = output -> {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                fileStream.close();
            };
            
            return Response.ok(stream)
                    .header("Content-Disposition", "attachment; filename=\"" + history.getFileName() + "\"")
                    .header("Content-Type", "application/json")
                    .build();
                    
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to download file: " + e.getMessage())).build();
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
