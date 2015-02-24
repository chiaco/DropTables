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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.codehaus.groovy.control.CompilationFailedException;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonProperty;

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
  @Property("groovyTemplate")
  String template;

  @NotEmpty
  @Property("groovyScript")
  String script;

  @Valid
  @NotNull
  @Property("defaultParameters")
  Map<String, String> bindings;

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

  public String getTemplate() {
    return template;
  }

  @JsonProperty("groovyTemplate")
  public void setTemplate(String template) throws CompilationFailedException, ClassNotFoundException, IOException {
    new SimpleTemplateEngine().createTemplate(template);

    this.template = template;
  }

  public Template parseTemplate() {
    try {
      return new SimpleTemplateEngine().createTemplate(template);
    } catch (CompilationFailedException | ClassNotFoundException | IOException e) {
      return null;
    }
  }

  public String getScript() {
    return script;
  }

  @JsonProperty("groovyScript")
  public void setScript(String script) throws CompilationFailedException {
    new GroovyShell().parse(script);

    this.script = script;
  }

  public Script parseScript() {
    try {
      return new GroovyShell().parse(script);
    } catch (CompilationFailedException e) {
      return null;
    }
  }

  public String writeScript(String cacheDir) {
    if (id == null) {
      return null;
    }

    String filename = id + ".groovy";
    try {
      FileUtils.writeStringToFile(new File(cacheDir, filename), script);
    } catch (IOException e) {
      return null;
    }

    return filename;
  }

  public Map<String, String> getParams() {
    return Collections.unmodifiableMap(bindings);
  }

  @JsonProperty("defaultParameters")
  @SuppressWarnings("unchecked")
  public void setParams(Map<String, String> bindings) throws InstantiationException, IllegalAccessException {
    this.bindings = bindings.getClass().newInstance();
    this.bindings.putAll(bindings);
  }

  public Binding parseBinding() {
    Binding binding = new Binding();
    for (Entry<String, String> entry : bindings.entrySet()) {
      binding.setVariable(entry.getKey(), entry.getValue());
    }

    return binding;
  }
}
