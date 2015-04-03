package com.callidusrobotics.droptables.auth;

import io.dropwizard.auth.UnauthorizedHandler;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SimpleUnauthorizedHandler implements UnauthorizedHandler {
//    private static final String CHALLENGE_FORMAT = "%s realm=\"%s\"";
    private static final String CHALLENGE_FORMAT = "Please enter your credit card and SSN";

    @Override
    public Response buildResponse(String prefix, String realm) {
        return Response.status(Response.Status.UNAUTHORIZED)
//                .header(HttpHeaders.WWW_AUTHENTICATE, String.format(CHALLENGE_FORMAT, prefix, realm))
                .header(HttpHeaders.WWW_AUTHENTICATE, CHALLENGE_FORMAT)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .entity("Every time you guess a wrong password, I drop a table...")
                .build();
    }
}
