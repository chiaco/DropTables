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

import io.dropwizard.setup.Environment;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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

import com.callidusrobotics.droptables.configuration.DropTablesConfig;
import com.callidusrobotics.droptables.model.DocumentDao;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Path("/collections/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentsResource {
  private DocumentDao dao;

  public DocumentsResource(DropTablesConfig config, Environment env) throws UnknownHostException {
    dao = new DocumentDao(config.getMongoFactory().buildDatastore(env));
  }

  @GET
  public List<String> list() {
    return new ArrayList<String>(dao.getCollectionNames());
  }

  @Path("/{collection}/")
  @GET
  public List<String> list(@PathParam("collection") String collection) {
    return dao.getDistinctValues(collection, DocumentDao.DOC_ID);
  }

  @Path("/{collection}/documents/")
  @POST
  public boolean upsert(@PathParam("collection") String collection, @Valid BasicDBObject document) {
    return dao.upsertDocument(collection, document);
  }

  @Path("/{collection}/documents/{id}/")
  @GET
  public DBObject fetch(@PathParam("collection") String collection, @PathParam("id") String id) {
    DBObject result = dao.getDocument(collection, id);
    if (result == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    return result;
  }

  @Path("/{collection}/documents/{id}/")
  @DELETE
  public boolean delete(@PathParam("collection") String collection, @PathParam("id") String id) {
    return dao.deleteDocument(collection, id);
  }
}
