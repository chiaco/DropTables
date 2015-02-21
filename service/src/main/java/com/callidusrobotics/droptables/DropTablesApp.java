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

package com.callidusrobotics.droptables;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.net.UnknownHostException;

import com.callidusrobotics.droptables.configuration.DropTablesConfig;
import com.callidusrobotics.droptables.health.MongoHealthCheck;
import com.callidusrobotics.droptables.resource.DocumentsResource;

public class DropTablesApp extends Application<DropTablesConfig> {

  public static void main(String[] args) throws Exception {
    new DropTablesApp().run(args);
  }

  @Override
  public void initialize(Bootstrap<DropTablesConfig> bootstrap) {}

  @Override
  public void run(DropTablesConfig config, Environment environment) throws UnknownHostException {
    environment.healthChecks().register("mongo", new MongoHealthCheck(config, environment));

    environment.jersey().register(new DocumentsResource(config, environment));
  }
}
