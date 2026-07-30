package com.matrixagents.controller;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.io.InputStream;

/**
 * Serves index.html for React Router paths so deep links and refreshes work in the packaged app.
 */
@Provider
public class SpaRoutingMapper implements ExceptionMapper<NotFoundException> {

    private static final String INDEX = "META-INF/resources/index.html";

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(NotFoundException exception) {
        // RESTEasy may report this with or without a leading slash.
        String path = uriInfo.getPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // Keep API, WebSocket, management, and asset requests as genuine 404s.
        if (path.startsWith("/api") || path.startsWith("/ws") || path.startsWith("/q/") || path.contains(".")) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        InputStream index = Thread.currentThread().getContextClassLoader().getResourceAsStream(INDEX);
        if (index == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(index, MediaType.TEXT_HTML_TYPE).build();
    }
}
