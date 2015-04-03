package com.callidusrobotics.droptables.auth;

import io.dropwizard.auth.Auth;
import io.dropwizard.setup.Environment;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.callidusrobotics.droptables.configuration.DropTablesConfig;

@Path("/secured_hello/")
@Produces(MediaType.APPLICATION_JSON)
public class SecuredHelloResource {
	
	public SecuredHelloResource(DropTablesConfig config, Environment env){
		
	}

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getGreeting(@Auth User user) {
        return "Hello world!";
    }
}