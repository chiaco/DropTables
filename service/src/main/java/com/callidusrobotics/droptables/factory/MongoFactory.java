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

package com.callidusrobotics.droptables.factory;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoFactory {
  private enum LockFlags {
    MONGO_CLIENT,
    DATASTORE
  }

  private transient final Lock[] locks = new Lock[LockFlags.values().length];
  private transient final Morphia morphia = new Morphia();
  private transient MongoClient mongoClient;
  private transient Datastore datastore;

  @NotEmpty
  private String dbName;

  @NotEmpty
  private String host;

  @Min(1)
  @Max(65535)
  private int port;

  private String username;
  private String password;

  public MongoFactory() {
    for (int i = 0; i < locks.length; i++) {
      locks[i] = new ReentrantLock();
    }
  }

  @JsonProperty
  public String getDbName() {
    return dbName;
  }

  @JsonProperty
  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  @JsonProperty
  public String getHost() {
    return host;
  }

  @JsonProperty
  public void setHost(String host) {
    this.host = host;
  }

  @JsonProperty
  public int getPort() {
    return port;
  }

  @JsonProperty
  public void setPort(int port) {
    this.port = port;
  }

  @JsonProperty
  public String getUsername() {
    return username;
  }

  @JsonProperty
  public void setUsername(String username) {
    this.username = username;
  }

  @JsonProperty
  public String getPassword() {
    return password;
  }

  @JsonProperty
  public void setPassword(String password) {
    this.password = password;
  }

  public Morphia getMorphia() {
    return morphia;
  }

  public MongoClient buildClient(Environment env) throws UnknownHostException {
    if (mongoClient != null) {
      return mongoClient;
    }

    Lock lock = locks[LockFlags.MONGO_CLIENT.ordinal()];
    lock.lock();

    try {
      if (mongoClient == null) {
        if (StringUtils.isBlank(username)) {
          mongoClient = new MongoClient(new ServerAddress(host, port));
        } else {
          char[] passwordChars = password == null ? new char[0] : password.toCharArray();
          mongoClient = new MongoClient(new ServerAddress(host, port), Arrays.asList(MongoCredential.createCredential(username, dbName, passwordChars)));
        }

        env.lifecycle().manage(new Managed() {
          @Override
          public void start() throws Exception {}

          @Override
          public void stop() throws Exception {
            mongoClient.close();
          }
        });
      }
    } finally {
      lock.unlock();
    }

    return mongoClient;
  }

  public Datastore buildDatastore(Environment env) throws UnknownHostException {
    if (datastore != null) {
      return datastore;
    }

    Lock lock = locks[LockFlags.DATASTORE.ordinal()];
    lock.lock();

    try {
      if (datastore == null) {
        datastore = morphia.createDatastore(buildClient(env), dbName);
      }
    } finally {
      lock.unlock();
    }

    return datastore;
  }
}
