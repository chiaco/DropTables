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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

/**
 * DAO to manage arbitrary documents within any collection in the database.
 *
 * @author Rusty Gerard
 * @since 0.0.1
 */
// Don't extend BasicDao (org.mongodb.morphia.dao.DAO assumes a 1-to-1 mapping of Entity to DBCollection objects)
public class DocumentDao {
  public static final String DOC_ID = "_id";
  private Datastore datastore;

  public DocumentDao(Datastore datastore) {
    this.datastore = datastore;
  }

  /**
   * Fetches a document from a collection.
   *
   * @param collectionName The collection containing the document.
   * @param id The document ID
   * @return The document, nullable
   */
  public DBObject getDocument(String collectionName, ObjectId id) {
    DBCollection collection = getCollection(collectionName);
    DBCursor cursor = collection.find(makeRef(id));

    List<DBObject> results = getDocuments(cursor);
    if (results.size() > 0) {
      return results.get(0);
    }

    return null;
  }

  /**
   * Deletes a document from a collection.
   *
   * @param collectionName The collection containing the document.
   * @param id The document ID
   * @return True if the document was deleted, false otherwise
   */
  public WriteResult deleteDocument(String collectionName, ObjectId id) {
    DBCollection collection = getCollection(collectionName);
    return collection.remove(makeRef(id));
  }

  /**
   * Upserts a document into a collection.
   * Creates the collection if it does not exist.
   * Creates the document if it does not exist.
   *
   * @param collectionName The collection to upsert to
   * @param document The document to upsert
   * @return ObjectId of the upserted document
   */
  public ObjectId upsertDocument(String collectionName, DBObject document) {
    DBCollection collection = getCollection(collectionName);
    collection.insert(document);

    return (ObjectId) document.get(DOC_ID);
  }

  /**
   * Finds distinct values for a specified key among documents in a collection.
   *
   * @param collectionName The collection to search
   * @param key The document key to select on
   * @return A list of distinct values, never null
   */
  public List<String> getDistinctValues(String collectionName, String key) {
    DBCollection collection = getCollection(collectionName);
    List<String> result = new LinkedList<String>();
    for (Object value : collection.distinct(key)) {
      result.add(value.toString());
    }

    return result;
  }

  /**
   * Finds documents containing the desired key,value pair.
   *
   * @param collectionName The collection to search
   * @param key The document key to select on
   * @param value The desired value
   * @return A list of matching documents, never null
   */
  public List<DBObject> getDocumentsByField(String collectionName, String key, String value) {
    DBCollection collection = getCollection(collectionName);
    DBObject query = QueryBuilder.start().and(key).is(value).get();
    DBCursor cursor = collection.find(query);

    return getDocuments(cursor);
  }

  /**
   * Finds all documents.
   *
   * @param collectionName The collection to search
   * @return A list of matching documents, never null
   */
  public List<DBObject> getAllDocuments(String collectionName) {
    DBCollection collection = getCollection(collectionName);
    DBCursor cursor = collection.find();

    return getDocuments(cursor);
  }

  protected List<DBObject> getDocuments(DBCursor cursor) {
    List<DBObject> result = new LinkedList<DBObject>();
    try {
      while (cursor.hasNext()) {
        result.add(flatten(cursor.next()));
      }
    } finally {
      cursor.close();
    }

    return result;
  }

  /**
   * Generates a list of collections in the database, excluding the system collections.
   *
   * @return A set of collection names, never null
   */
  public Set<String> getCollectionNames() {
    Set<String> result = getDatastore().getDB().getCollectionNames();
    for (Iterator<String> iterator = result.iterator(); iterator.hasNext();) {
      String collection = iterator.next();
      if (collection.startsWith("system.")) {
        iterator.remove();
      }
    }

    return result;
  }

  public DBCollection getCollection(String collection) {
    return getDatastore().getDB().getCollection(collection);
  }

  public Datastore getDatastore() {
    return datastore;
  }

  // Flatten the DOC_ID into its serialized form so that the parser doesn't create an ObjectID object
  protected static DBObject flatten(DBObject item) {
    item.put(DOC_ID, item.get(DOC_ID).toString());
    return item;
  }

  protected static DBObject makeRef(ObjectId id) {
    BasicDBObject document = new BasicDBObject();
    document.put(DOC_ID, id);

    return document;
  }
}
