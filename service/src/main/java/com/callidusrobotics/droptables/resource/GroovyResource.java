/**
 * Copyright (C) 2015 Rusty Gerard
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.callidusrobotics.droptables.resource;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import io.dropwizard.setup.Environment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;

import com.callidusrobotics.droptables.configuration.DropTablesConfig;
import com.callidusrobotics.droptables.model.DocumentDao;
import com.callidusrobotics.droptables.model.GroovyDao;
import com.callidusrobotics.droptables.model.GroovyScript;

@Path("/scripts/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroovyResource {
  private final GroovyDao dao;
  private final String scriptsCacheDir;
  private final GroovyScriptEngine engine;

  public GroovyResource(DropTablesConfig config, Environment env) throws IOException {
    dao = new GroovyDao(config.getMongoFactory().buildDatastore(env));
    scriptsCacheDir = config.getScriptsCacheDir();
    String[] roots = {scriptsCacheDir};

    engine = new GroovyScriptEngine(roots);
  }

  @GET
  public List<String> list() {
    List<ObjectId> ids = dao.findIds();
    List<String> result = new ArrayList<String>(ids.size());
    for (ObjectId id : ids) {
      result.add(id.toString());
    }

    return result;
  }

  @POST
  public String upsert(@Valid GroovyScript entity) {
    return dao.save(entity).getId().toString() + "\n";
  }

  @Path("/{id}/")
  @GET
  public GroovyScript fetch(@PathParam("id") ObjectId id) {
    return dao.get(id);
  }

  @Path("/{id}/")
  @DELETE
  public boolean delete(@PathParam("id") ObjectId id) {
    return dao.deleteById(id) != null;
  }

  // FIXME: Need to create a mapper to return error messages as TEXT/HTML when we throw WebApplicationException
  @Produces(MediaType.TEXT_HTML)
  @Path("/{id}/results/")
  @POST
  public String execute(@PathParam("id") ObjectId id, @Valid Map<String, String> scriptBindings) {
    // Fetch the script from the database
    GroovyScript script = dao.get(id);
    if (!script.write(scriptsCacheDir)) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    // Set key-value pairs in the request body as variable bindings for the script
    Binding binding = new Binding();
    for (Entry<String, String> entry : scriptBindings.entrySet()) {
      binding.setVariable(entry.getKey(), entry.getValue());
    }

    // Give the script access to Mongo
    // FIXME: Need to initialize the DAO with a list of collections that the script has access to
    DocumentDao docDao = new DocumentDao(dao.getDatastore());
    binding.setVariable("DAO", docDao);

    // Execute the script
    try {
      engine.run(id + ".json", binding);
    } catch (ResourceException | ScriptException e) {
      throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
    }

    // All done
    if (binding.hasVariable("OUTPUT") && binding.getVariable("OUTPUT") != null) {
      return binding.getVariable("OUTPUT").toString() + "\n";
    }

    return "";
  }
}
