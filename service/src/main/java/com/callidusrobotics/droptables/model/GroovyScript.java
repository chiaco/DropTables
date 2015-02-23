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

import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.codehaus.groovy.control.CompilationFailedException;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;

@Entity("groovy")
public class GroovyScript {

  @Id
  ObjectId id;

  @Property("dateCreated")
  Date created;
  
  @Property("dateModified")
  Date modified;

  @NotEmpty
  @Property("name")
  String name;

  @NotEmpty
  @Property("author")
  String author;

  @NotEmpty
  @Property("data")
  String data;

  public ObjectId getId() {
    return id;
  }

  public Date getCreated() {
    if (created == null) {
      return null;
    }

    return new Date(created.getTime());
  }

  public Date getModified() {
    if (modified == null) {
      return null;
    }

    return new Date(modified.getTime());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getData() {
    return data;
  }

  @PrePersist
  void prePersist() {
    // FIXME: These dates should use Mongo's currentDate
    // Maybe we could register GroovyDao as an @EntityListeners ?
    if (created == null) {
      created = modified = new Date();
    } else {
      modified = new Date();
    }
  }

  public void setData(String data) throws CompilationFailedException {
    new GroovyShell().parse(data);

    this.data = data;
  }

  public boolean write(String cacheDir) {
    if (id == null) {
      return false;
    }

    String filename = id + ".json";
    try {
      FileUtils.writeStringToFile(new File(cacheDir, filename), data);
    } catch (IOException e) {
      return false;
    }

    return true;
  }
}
