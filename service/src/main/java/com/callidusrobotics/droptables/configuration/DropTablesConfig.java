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

package com.callidusrobotics.droptables.configuration;

import java.io.File;

import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Top-level configuration class.
 *
 * @author Rusty Gerard
 * @since 0.0.1
 */
public class DropTablesConfig extends Configuration {
  private static final Logger LOGGER = LoggerFactory.getLogger(DropTablesConfig.class);

  @NotEmpty
  private String scriptsCacheDir = "/tmp/droptables/groovy/";

  private boolean prettyPrint = true;

  @Valid
  @NotNull
  private MongoFactory mongoFactory;

  @JsonProperty
  public String getScriptsCacheDir() {
    return scriptsCacheDir;
  }

  @JsonProperty
  public void setScriptsCacheDir(String scriptsCacheDir) {
    if (! new File(scriptsCacheDir).mkdirs()) {
      LOGGER.error("Unable to create directory: " + scriptsCacheDir);
    }

    this.scriptsCacheDir = scriptsCacheDir;
  }

  @JsonProperty
  public boolean isPrettyPrint() {
    return prettyPrint;
  }

  @JsonProperty
  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  @JsonCreator
  public DropTablesConfig(@JsonProperty("mongo") MongoFactory mongoFactory) {
    this.mongoFactory = mongoFactory;
  }

  @JsonProperty("mongo")
  public MongoFactory getMongoFactory() {
    return mongoFactory;
  }
  
  
  // New Auth test garbage
  @NotNull
  private String login;
  
  @NotNull
  private String password;

  @JsonProperty
  public String getLogin() {
      return login;
  }

  @JsonProperty
  public String getPassword() {
      return password;
  }
}
