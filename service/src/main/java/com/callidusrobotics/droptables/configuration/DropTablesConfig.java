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

import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.callidusrobotics.droptables.factory.MongoFactory;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DropTablesConfig extends Configuration {

  @Valid
  @NotNull
  private MongoFactory mongoFactory;

  @JsonCreator
  public DropTablesConfig(@JsonProperty("mongo") MongoFactory mongoFactory) {
    this.mongoFactory = mongoFactory;
  }

  @JsonProperty("mongo")
  public MongoFactory getMongoFactory() {
    return mongoFactory;
  }
}
