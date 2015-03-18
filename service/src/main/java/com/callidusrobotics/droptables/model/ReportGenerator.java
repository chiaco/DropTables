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
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.util.GroovyScriptEngine;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.bson.types.ObjectId;
import org.codehaus.groovy.control.CompilationFailedException;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO to store Groovy scripts, templates, and default variable bindings for
 * ad-hoc report generation.
 *
 * @author Rusty Gerard
 * @since 0.0.1
 */
@Entity("droptables.reports")
public class ReportGenerator {
  /**
   * Enumerated type of supported scripting languages.
   *
   * @author Rusty Gerard
   * @since 0.0.1
   */
  public enum Language {
    GROOVY
  }

  @Id
  ObjectId id;

  @Property("dateCreated")
  Date created;

  @Property("dateModified")
  Date modified;

  @NotEmpty
  @Property("name")
  String name;

  @NotNull
  @Property("description")
  String description;

  @NotEmpty
  @Property("author")
  String author;

  @Valid
  @NotNull
  @Property("language")
  Language language;

  @NotEmpty
  @Property("template")
  String template;

  @NotEmpty
  @Property("script")
  String script;

  @Valid
  @NotNull
  @Property("defaultParameters")
  Map<String, String> binding = Collections.emptyMap();

  @PrePersist
  void prePersist() {
    // FIXME: These dates should use Mongo's currentDate
    // Maybe we could register ReportDao as an @EntityListeners ?
    if (created == null) {
      created = modified = new Date();
    } else {
      modified = new Date();
    }
  }

  public ObjectId getId() {
    return id;
  }

  @JsonProperty("dateCreated")
  public Date getCreated() {
    if (created == null) {
      return null;
    }

    return new Date(created.getTime());
  }

  @JsonProperty("dateCreated")
  public void setCreated(Date created) {
    if (created != null) {
      this.created = new Date(created.getTime());
    }
  }

  @JsonProperty("dateModified")
  public Date getModified() {
    if (modified == null) {
      return null;
    }

    return new Date(modified.getTime());
  }

  @JsonProperty("dateModified")
  public void setModified(Date modified) {
    if (modified != null) {
      this.modified = new Date(modified.getTime());
    }
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  @JsonProperty("author")
  public String getAuthor() {
    return author;
  }

  @JsonProperty("author")
  public void setAuthor(String author) {
    this.author = author;
  }

  @JsonProperty("language")
  public Language getLanguage() {
    return language;
  }

  @JsonProperty("language")
  public void setLanguage(Language language) {
    Validate.notNull(language);
    this.language = language;
  }

  @JsonProperty("template")
  public String getTemplate() {
    return template;
  }

  @JsonProperty("template")
  public void setTemplate(String template) throws GroovyRuntimeException, ClassNotFoundException, IOException {
    new SimpleTemplateEngine().createTemplate(template);

    this.template = template;
  }

  public Template parseTemplate() throws CompilationFailedException, ClassNotFoundException, IOException {
    return new SimpleTemplateEngine().createTemplate(template);
  }

  @JsonProperty("script")
  public String getScript() {
    return script;
  }

  @JsonProperty("script")
  public void setScript(String script) throws CompilationFailedException {
    new GroovyShell().parse(script);

    this.script = script;
  }

  /**
   * Converts the String into a Script object.
   *
   * @return The Script object, never null
   * @throws CompilationFailedException
   */
  public Script parseScript() throws CompilationFailedException {
    return new GroovyShell().parse(script);
  }

  /**
   * Writes the String data to a file for use by a {@link GroovyScriptEngine}.
   *
   * @param cacheDir
   *          The directory to create the file in.
   * @return The name of the file that was written to.
   * @throws IOException
   */
  public String writeScript(String cacheDir) throws IOException {
    if (id == null) {
      throw new IOException("Unable to persist script: ID not set.");
    }

    String filename = id + ".groovy";
    FileUtils.writeStringToFile(new File(cacheDir, filename), script);

    return filename;
  }

  @JsonProperty("defaultParameters")
  public Map<String, String> getBinding() {
    return Collections.unmodifiableMap(binding);
  }

  @JsonProperty("defaultParameters")
  @SuppressWarnings("unchecked")
  public void setBinding(Map<String, String> binding) throws InstantiationException, IllegalAccessException {
    this.binding = binding.getClass().newInstance();
    this.binding.putAll(binding);
  }

  /**
   * Converts the Map of default parameters into a Binding object.
   *
   * @return The Binding, never null
   */
  public Binding parseBinding() {
    Binding result = new Binding();
    for (Entry<String, String> entry : binding.entrySet()) {
      result.setVariable(entry.getKey(), entry.getValue());
    }

    return result;
  }
}
