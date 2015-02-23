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

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.callidusrobotics.droptables.configuration.LoginInfo;
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
  private transient MongoClient rwClient, roClient;
  private transient Datastore rwDatastore, roDatastore;

  @NotEmpty
  private String dbName;

  @NotEmpty
  private String host;

  @Min(1)
  @Max(65535)
  private int port;

  // A Mongo user with read-write access is required
  @Valid
  @NotNull
  LoginInfo rwUser;

  // A Mongo user with read-only access is optional
  @Valid
  LoginInfo roUser;

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

  @JsonProperty("serviceUser")
  public void setRwUser(LoginInfo rwUser) {
    this.rwUser = rwUser;
  }

  @JsonProperty("reportsUser")
  public void setRoUser(LoginInfo roUser) {
    this.roUser = roUser;
  }

  public Morphia getMorphia() {
    return morphia;
  }

  public MongoClient buildReadWriteClient(Environment env) throws UnknownHostException {
    if (rwClient != null) {
      return rwClient;
    }

    Lock lock = locks[LockFlags.MONGO_CLIENT.ordinal()];
    lock.lock();

    try {
      if (rwClient == null) {
        rwClient = buildClient(env, rwUser.getUsername(), rwUser.getPassword());
      }
    } finally {
      lock.unlock();
    }

    return rwClient;
  }

  public MongoClient buildReadOnlyClient(Environment env) throws UnknownHostException {
    if (roClient != null) {
      return roClient;
    }

    if (roUser == null || rwUser.equals(roUser)) {
      roClient = buildReadWriteClient(env);
    }

    Lock lock = locks[LockFlags.MONGO_CLIENT.ordinal()];
    lock.lock();

    try {
      if (roClient == null && roUser != null) {
        roClient = buildClient(env, roUser.getUsername(), roUser.getPassword());
      }
    } finally {
      lock.unlock();
    }

    return roClient;
  }

  private MongoClient buildClient(Environment env, String username, String password) throws UnknownHostException {
    final MongoClient mongoClient;
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

    return mongoClient;
  }

  public Datastore buildReadWriteDatastore(Environment env) throws UnknownHostException {
    if (rwDatastore != null) {
      return rwDatastore;
    }

    Lock lock = locks[LockFlags.DATASTORE.ordinal()];
    lock.lock();

    try {
      if (rwDatastore == null) {
        rwDatastore = morphia.createDatastore(buildReadWriteClient(env), dbName);
      }
    } finally {
      lock.unlock();
    }

    return rwDatastore;
  }

  public Datastore buildReadOnlyDatastore(Environment env) throws UnknownHostException {
    if (roDatastore != null) {
      return roDatastore;
    }

    if (roUser == null || rwUser.equals(roUser)) {
      roDatastore = buildReadWriteDatastore(env);
    }

    Lock lock = locks[LockFlags.DATASTORE.ordinal()];
    lock.lock();

    try {
      if (roDatastore == null) {
        roDatastore = morphia.createDatastore(buildReadOnlyClient(env), dbName);
      }
    } finally {
      lock.unlock();
    }

    return roDatastore;
  }
}
