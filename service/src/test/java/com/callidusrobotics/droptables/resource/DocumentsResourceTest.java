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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.callidusrobotics.droptables.model.DocumentDao;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@RunWith(MockitoJUnitRunner.class)
public class DocumentsResourceTest {
  DocumentsResource resource;

  @Mock DocumentDao mockDao;
  @Mock ObjectId mockId;

  static final String COLLECTION_NAME = "testCollection";

  @Before
  public void before() throws Exception {
    resource = new DocumentsResource(mockDao);
  }

  @After
  public void after() throws Exception {
    verifyNoMoreInteractions(mockDao);
  }

  @Test
  public void fetchSuccess() throws Exception {
    when(mockDao.getDocument(COLLECTION_NAME, mockId)).thenReturn(new BasicDBObject());

    DBObject result = resource.fetch(COLLECTION_NAME, mockId);

    verify(mockDao).getDocument(COLLECTION_NAME, mockId);

    assertNotNull(result);
  }

  @Test(expected = WebApplicationException.class)
  public void fetchFailureNotFound() throws Exception {
    when(mockDao.getDocument("testCollection", mockId)).thenReturn(null);

    try {
      resource.fetch(COLLECTION_NAME, mockId);
    } catch (WebApplicationException e) {
      verify(mockDao).getDocument(COLLECTION_NAME, mockId);

      assertEquals(Response.Status.NOT_FOUND, Status.fromStatusCode(e.getResponse().getStatus()));

      throw e;
    }
  }
}
