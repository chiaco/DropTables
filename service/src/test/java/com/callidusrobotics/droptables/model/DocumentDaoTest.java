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

package com.callidusrobotics.droptables.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mongodb.morphia.Datastore;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

@RunWith(MockitoJUnitRunner.class)
public class DocumentDaoTest {
  DocumentDao dao;

  static final String COLLECTION_NAME = "testCollection" + Math.random();

  static final Comparator<DBObject> COMPARATOR = new Comparator<DBObject>() {
    public int compare(DBObject obj1, DBObject obj2) {
      return obj1.hashCode() - obj2.hashCode();
    }
  };

  @Mock Datastore mockDatastore;
  @Mock DB mockDb;
  @Mock DBCollection mockCollection;
  @Mock DBCursor mockCursor;
  @Mock ObjectId mockId, mockId2;
  @Mock DBObject mockObject, mockObject2;
  @Mock WriteResult mockWriteResult;

  @Before
  public void before() throws Exception {
    dao = new DocumentDao(mockDatastore);

    when(mockDatastore.getDB()).thenReturn(mockDb);
    when(mockDb.getCollection(COLLECTION_NAME)).thenReturn(mockCollection);
    when(mockCollection.find(isA(DBObject.class))).thenReturn(mockCursor);
    when(mockObject.get(DocumentDao.DOC_ID)).thenReturn(mockId);
    when(mockObject2.get(DocumentDao.DOC_ID)).thenReturn(mockId2);
    when(mockId.toString()).thenReturn("11111");
    when(mockId2.toString()).thenReturn("22222");
  }

  @After
  public void after() throws Exception {
    verifyNoMoreInteractions(mockDatastore);
    verifyNoMoreInteractions(mockDb);
    verifyNoMoreInteractions(mockCollection);
    verifyNoMoreInteractions(mockCursor);
  }

  @Test
  public void fetchDocumentSuccessOneResult() {
    when(mockCursor.hasNext()).thenReturn(true).thenReturn(false);
    when(mockCursor.next()).thenReturn(mockObject).thenReturn(null);

    // Unit under test
    DBObject result = dao.getDocument(COLLECTION_NAME, mockId);

    // Verify results
    verify(mockDatastore).getDB();
    verify(mockDb).getCollection(COLLECTION_NAME);
    verify(mockCollection).find(isA(DBObject.class));
    verify(mockCursor, times(2)).hasNext();
    verify(mockCursor).next();
    verify(mockCursor).close();

    assertEquals(mockObject, result);
  }

  @Test
  public void fetchDocumentSuccessMultipleResults() throws Exception {
    when(mockCursor.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(mockCursor.next()).thenReturn(mockObject2).thenReturn(mockObject).thenReturn(null);

    // Unit under test
    DBObject result = dao.getDocument(COLLECTION_NAME, mockId);

    // Verify results
    verify(mockDatastore).getDB();
    verify(mockDb).getCollection(COLLECTION_NAME);
    verify(mockCollection).find(isA(DBObject.class));
    verify(mockCursor, times(3)).hasNext();
    verify(mockCursor, times(2)).next();
    verify(mockCursor).close();

    assertEquals(mockObject2, result);
  }

  @Test
  public void fetchDocumentSuccessNoResults() throws Exception {
    when(mockCursor.hasNext()).thenReturn(false);
    when(mockCursor.next()).thenThrow(new NoSuchElementException("Result set is empty"));

    // Unit under test
    DBObject result = dao.getDocument(COLLECTION_NAME, mockId);

    // Verify results
    verify(mockDatastore).getDB();
    verify(mockDb).getCollection(COLLECTION_NAME);
    verify(mockCollection).find(isA(DBObject.class));
    verify(mockCursor).hasNext();
    verify(mockCursor).close();

    assertNull("Result should not exist", result);
  }

  @Test
  public void deleteDocumentSuccess() throws Exception {
    when(mockCollection.remove(isA(DBObject.class))).thenReturn(mockWriteResult);

    // Unit under test
    WriteResult result = dao.deleteDocument(COLLECTION_NAME, mockId);

    // Verify results
    verify(mockDatastore).getDB();
    verify(mockDb).getCollection(COLLECTION_NAME);
    verify(mockCollection).remove(isA(DBObject.class));

    assertEquals(mockWriteResult, result);
  }

  @Test
  public void upsertDocumentSuccess() throws Exception {
    when(mockCollection.insert(mockObject)).thenReturn(mockWriteResult);

    // Unit under test
    ObjectId result = dao.upsertDocument(COLLECTION_NAME, mockObject);

    // Verify results
    verify(mockDatastore).getDB();
    verify(mockDb).getCollection(COLLECTION_NAME);
    verify(mockCollection).insert(mockObject);

    assertEquals(mockId, result);
  }

  @Test
  public void getDistinctValuesByDocIdSuccess() throws Exception {
    List<String> values = Arrays.asList(mockId.toString(), mockId2.toString());
    when(mockCollection.distinct(DocumentDao.DOC_ID)).thenReturn(values);

    // Unit under test
    List<String> result = dao.getDistinctValues(COLLECTION_NAME, DocumentDao.DOC_ID);

    // Verify results
    verify(mockDatastore).getDB();
    verify(mockDb).getCollection(COLLECTION_NAME);
    verify(mockCollection).distinct(DocumentDao.DOC_ID);

    Collections.sort(values);
    Collections.sort(result);
    assertEquals(values, result);
  }

  @Test
  public void getDocumentsByFieldByDocIdSuccess() throws Exception {
    when(mockCollection.find(isA(DBObject.class))).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(true).thenReturn(false);
    when(mockCursor.next()).thenReturn(mockObject).thenReturn(null);

    // Unit under test
    List<DBObject> result = dao.getDocumentsByField(COLLECTION_NAME, DocumentDao.DOC_ID, mockId.toString());

    // Verify results
    verify(mockDatastore).getDB();
    verify(mockDb).getCollection(COLLECTION_NAME);
    verify(mockCollection).find(isA(DBObject.class));
    verify(mockCursor, times(2)).hasNext();
    verify(mockCursor).next();
    verify(mockCursor).close();

    assertEquals(Arrays.asList(mockObject), result);
  }

  @Test
  public void getAllDocumentsSuccess() throws Exception {
    List<DBObject> values = Arrays.asList(mockObject, mockObject2);
    when(mockCollection.find()).thenReturn(mockCursor);
    when(mockCursor.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(mockCursor.next()).thenReturn(mockObject2).thenReturn(mockObject).thenReturn(null);

    // Unit under test
    List<DBObject> result = dao.getAllDocuments(COLLECTION_NAME);

    // Verify results
    verify(mockDatastore).getDB();
    verify(mockDb).getCollection(COLLECTION_NAME);
    verify(mockCollection).find();
    verify(mockCursor, times(3)).hasNext();
    verify(mockCursor, times(2)).next();
    verify(mockCursor).close();

    Collections.sort(values, COMPARATOR);
    Collections.sort(result, COMPARATOR);
    assertEquals(values, result);
  }

  @Test
  public void getCollectionNamesSuccess() throws Exception {
    Set<String> values = new LinkedHashSet<String>(Arrays.asList(COLLECTION_NAME, "groovy", "system.namespaces", "system.indexes", "system.profile", "system.js"));
    when(mockDb.getCollectionNames()).thenReturn(values);

    // Unit under test
    Set<String> result = dao.getCollectionNames();

    // Verify results
    verify(mockDatastore).getDB();
    verify(mockDb).getCollectionNames();

    assertEquals(new LinkedHashSet<String>(Arrays.asList(COLLECTION_NAME, "groovy")), result);
  }
}
