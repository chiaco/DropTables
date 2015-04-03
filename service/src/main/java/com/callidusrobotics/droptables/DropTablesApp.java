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
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

import java.io.IOException;

import com.callidusrobotics.droptables.auth.GreetingAuthenticator;
import com.callidusrobotics.droptables.auth.SecuredHelloResource;
import com.callidusrobotics.droptables.auth.SimpleUnauthorizedHandler;
import com.callidusrobotics.droptables.auth.User;
import com.callidusrobotics.droptables.configuration.DropTablesConfig;
import com.callidusrobotics.droptables.exception.HtmlBodyErrorWriter;
import com.callidusrobotics.droptables.health.FileSystemHealthCheck;
import com.callidusrobotics.droptables.health.MongoHealthCheck;
import com.callidusrobotics.droptables.resource.DocumentsResource;
import com.callidusrobotics.droptables.resource.ReportsResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;

/**
 * Main method for spinning up the HTTP server.
 *
 * @author Rusty Gerard
 * @since 0.0.1
 */
public class DropTablesApp extends Application<DropTablesConfig> {

  public static void main(String[] args) throws Exception {
    new DropTablesApp().run(args);
  }

  @Override
  public void initialize(Bootstrap<DropTablesConfig> bootstrap) {
    bootstrap.addBundle(new ViewBundle<DropTablesConfig>() {
      @Override
      public ImmutableMap<String, ImmutableMap<String, String>> getViewConfiguration(DropTablesConfig config) {
        return ImmutableMap.of(".ftl", ImmutableMap.of("strict_syntax", "yes", "whitespace_stripping", "yes"));
      }
    });
    bootstrap.addBundle(new AssetsBundle("/assets/css", "/css", null, "css"));
  }

  @Override
  public void run(DropTablesConfig config, Environment environment) throws IOException {
    if (config.isPrettyPrint()) {
      ObjectMapper mapper = environment.getObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // Error Writers
    environment.jersey().register(new HtmlBodyErrorWriter());

    // Health checks
    environment.healthChecks().register("mongo", new MongoHealthCheck(config, environment));
    environment.healthChecks().register("fileSystem", new FileSystemHealthCheck(config, environment));

    // Resources
    environment.jersey().register(new DocumentsResource(config, environment));
    environment.jersey().register(new ReportsResource(config, environment));
    environment.jersey().register(new SecuredHelloResource(config, environment));

    // Auth stuff
    GreetingAuthenticator greetingAuthenticator = new GreetingAuthenticator(config.getLogin(), config.getPassword());
    BasicAuthFactory<User> authFactory = new BasicAuthFactory<>(greetingAuthenticator, "DropTablesAuthTest", User.class);
    SimpleUnauthorizedHandler unauthorizedHandler = new SimpleUnauthorizedHandler();
    authFactory.responseBuilder(unauthorizedHandler);
    environment.jersey().register(AuthFactory.binder(authFactory));

    
    //environment.jersey().setUrlPattern("/css/*");
  }
}
