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
import groovy.lang.GroovyRuntimeException;
import groovy.text.Template;
import groovy.util.GroovyScriptEngine;
import io.dropwizard.auth.Auth;
import io.dropwizard.setup.Environment;

import java.io.IOException;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import org.eclipse.jetty.server.Authentication.User;
import org.mongodb.morphia.Datastore;

import com.callidusrobotics.droptables.configuration.DropTablesConfig;
import com.callidusrobotics.droptables.configuration.MongoFactory;
import com.callidusrobotics.droptables.exception.HtmlWebApplicationException;
import com.callidusrobotics.droptables.model.DocumentDao;
import com.callidusrobotics.droptables.model.ReportDao;
import com.callidusrobotics.droptables.model.ReportGenerator;
import com.callidusrobotics.droptables.view.ReportEditView;
import com.callidusrobotics.droptables.view.ReportExecuteView;
import com.callidusrobotics.droptables.view.ReportListView;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.mongodb.WriteResult;

/**
 * REST APIs to perform CRUDE operations on Groovy objects for ad-hoc report
 * generation.
 *
 * @author Rusty Gerard
 * @since 0.0.1
 * @see ReportDao
 * @see ReportGenerator
 */
@Path("/reports/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportsResource {
  private final ReportDao dao;
  private final Datastore roDatastore;
  private final String scriptsCacheDir;
  private final GroovyScriptEngine scriptEngine;

  // Constructor for unit tests
  ReportsResource(ReportDao dao, Datastore roDatastore, GroovyScriptEngine scriptEngine, String scriptsCacheDir) {
    this.dao = dao;
    this.roDatastore = roDatastore;
    this.scriptEngine = scriptEngine;
    this.scriptsCacheDir = scriptsCacheDir;
  }

  public ReportsResource(DropTablesConfig config, Environment env) throws IOException {
    dao = new ReportDao(config.getMongoFactory().buildReadWriteDatastore(env));
    roDatastore = config.getMongoFactory().buildReadOnlyDatastore(env);
    scriptsCacheDir = config.getScriptsCacheDir();
    String[] roots = { scriptsCacheDir };

    scriptEngine = new GroovyScriptEngine(roots);
  }

  /**
   * Generates a View listing ReportGenerator objects in the DB.
   *
   * @return The View, never null
   */
  @Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
  @GET
  public ReportListView list() {
    List<ReportGenerator> reportGenerators = dao.find().asList();
    return new ReportListView(reportGenerators);
  }

  @POST
  public Map<String, String> upsert(@Valid ReportGenerator entity) {
    return ImmutableMap.of(DocumentDao.DOC_ID, dao.save(entity).getId().toString());
  }

  /**
   * Generates a View that allows the user to create a new ReportGenerator.
   *
   * @return The View, never null
   */
  @Produces(MediaType.TEXT_HTML)
  @Path("/new/")
  @GET
  public ReportEditView create() {
    return new ReportEditView(new ReportGenerator());
  }

  /**
   * Generates a View that allows the user to edit an existing ReportGenerator.
   *
   * @param id
   *          The {@link ReportGenerator} object to fetch from the database and
   *          execute
   * @return The View, never null
   */
  @Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
  @Path("/{id}/")
  @GET
  public ReportEditView fetch(@Valid @PathParam("id") ObjectId id) {
    ReportGenerator reportGenerator = dao.get(id);
    if (reportGenerator == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    return new ReportEditView(reportGenerator);
  }

  /**
   * Deletes the specified ReportGenerator.
   *
   * @param id
   *          The {@link ReportGenerator} object to fetch from the database and
   *          execute
   * @return The WriteResult, never null
   */
  @Path("/{id}/")
  @DELETE
  public WriteResult delete(@Valid @PathParam("id") ObjectId id) {
    return dao.deleteById(id);
  }

  /**
   * Generates a View that allows the user to execute the ReportGenerator.
   *
   * @param id
   *          The {@link ReportGenerator} object to fetch from the database and
   *          execute
   * @return The View, never null
   */
  @Produces(MediaType.TEXT_HTML)
  @Path("/{id}/results/")
  @GET
  public ReportExecuteView execute(@Valid @PathParam("id") ObjectId id) {
    ReportGenerator reportGenerator = dao.get(id);
    if (reportGenerator == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    return new ReportExecuteView(reportGenerator);
  }

  /**
   * Generates an HTML report by executing a Groovy script that queries the
   * database.
   * <p>
   * Variable bindings are resolved in the following order:
   * <ol>
   * <li>Default parameters are loaded from the Groovy object in the database</li>
   * <li>Parameters in the HTTP request body override default parameters</li>
   * <li>The Groovy script is executed with the current variable bindings</li>
   * </ol>
   * The final set of variable bindings created or updated by the Groovy script
   * are supplied to the template engine to produce the HTML report.
   *
   * @param id
   *          The {@link ReportGenerator} object to fetch from the database and
   *          execute
   * @param scriptBindings
   *          Variable bindings (key-value pairs) to override the default
   *          bindings stored in the POJO
   * @return The HTML report generated by the Groovy template
   * @see MongoFactory#buildReadOnlyDatastore(Environment)
   */
  @Produces(MediaType.TEXT_HTML)
  @Path("/{id}/results/")
  @POST
  public String execute(@Valid @PathParam("id") ObjectId id, @Valid Map<String, String> scriptBindings) {
    // Fetch the script from the database
    ReportGenerator reportGenerator = dao.get(id);
    if (reportGenerator == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    Template template;
    Binding binding;
    String filename;
    try {
      reportGenerator.parseScript();
      template = reportGenerator.parseTemplate();
      binding = reportGenerator.parseBinding();
      filename = reportGenerator.writeScript(scriptsCacheDir);
    } catch (GroovyRuntimeException | IOException | ClassNotFoundException e) {
      throw new HtmlWebApplicationException(e, Response.Status.BAD_REQUEST);
    }

    // Set key-value pairs in the request body as variable bindings for the script
    for (Entry<String, String> entry : scriptBindings.entrySet()) {
      binding.setVariable(entry.getKey(), entry.getValue());
    }

    // Give the script read-only access to Mongo
    DocumentDao docDao = new DocumentDao(roDatastore);
    binding.setVariable("DAO", docDao);

    // Execute the script
    // Note that the script can throw any type of exception, not just the types declared by the engine's run method
    try {
      scriptEngine.run(filename, binding);
    } catch (Exception e) {
      throw new HtmlWebApplicationException(e, Response.Status.BAD_REQUEST);
    }

    // Process the template with the final binding
    try {
      return template.make(binding.getVariables()).toString();
    } catch (Exception e) {
      throw new HtmlWebApplicationException(e, Response.Status.BAD_REQUEST);
    }
  }
}
