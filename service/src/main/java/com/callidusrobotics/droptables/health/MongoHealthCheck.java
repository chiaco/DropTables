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

package com.callidusrobotics.droptables.health;

import io.dropwizard.setup.Environment;

import java.net.UnknownHostException;

import com.callidusrobotics.droptables.configuration.DropTablesConfig;
import com.codahale.metrics.health.HealthCheck;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class MongoHealthCheck extends HealthCheck {

  private final MongoClient rwClient, roClient;

  public MongoHealthCheck(DropTablesConfig config, Environment environment) throws UnknownHostException {
    super();

    rwClient = config.getMongoFactory().buildReadWriteClient(environment);
    roClient = config.getMongoFactory().buildReadOnlyClient(environment);
  }

  @Override
  protected Result check() throws Exception {
    try {
      rwClient.getDatabaseNames();
    } catch (MongoException e) {
      return Result.unhealthy("read-write client: " + e.getMessage());
    }

    try {
      roClient.getDatabaseNames();
    } catch (MongoException e) {
      return Result.unhealthy("read-only client: " + e.getMessage());
    }

    return Result.healthy();
  }
}
