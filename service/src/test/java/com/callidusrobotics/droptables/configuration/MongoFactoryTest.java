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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mongodb.morphia.Datastore;

import com.callidusrobotics.droptables.configuration.LoginInfo;
import com.callidusrobotics.droptables.configuration.MongoFactory;
import com.mongodb.MongoClient;

@RunWith(MockitoJUnitRunner.class)
public class MongoFactoryTest {
  MongoFactory factory;

  @Mock Environment mockEnv;
  @Mock LifecycleEnvironment mockLifecycle;

  @Before
  public void before() throws Exception {
    factory = new MongoFactory();

    factory.setHost("localhost");
    factory.setPort(65535);
    factory.setDbName("MongoFactoryUnitTests");

    when(mockEnv.lifecycle()).thenReturn(mockLifecycle);
  }

  @Test
  public void buildClientsSuccesNoReadOnly() throws Exception {
    factory.setRwUser(new LoginInfo("", ""));
    factory.setRoUser(null);

    MongoClient rwClient = factory.buildReadWriteClient(mockEnv);
    MongoClient roClient = factory.buildReadOnlyClient(mockEnv);

    assertEquals(roClient, rwClient);
  }

  @Test
  public void buildClientsSuccesSameCredentials() throws Exception {
    factory.setRwUser(new LoginInfo("test", "test"));
    factory.setRoUser(new LoginInfo("test", "test"));

    MongoClient rwClient = factory.buildReadWriteClient(mockEnv);
    MongoClient roClient = factory.buildReadOnlyClient(mockEnv);

    assertEquals(roClient, rwClient);
  }

  @Test
  public void buildClientsSuccessDifferentCredentials() throws Exception {
    factory.setRwUser(new LoginInfo("test", "test"));
    factory.setRoUser(new LoginInfo("", ""));

    MongoClient rwClient = factory.buildReadWriteClient(mockEnv);
    MongoClient roClient = factory.buildReadOnlyClient(mockEnv);

    assertNotEquals(roClient, rwClient);
  }

  @Test
  public void buildDatastoresSuccesNoReadOnly() throws Exception {
    factory.setRwUser(new LoginInfo("", ""));
    factory.setRoUser(null);

    Datastore rwDatastore = factory.buildReadWriteDatastore(mockEnv);
    Datastore roDatastore = factory.buildReadOnlyDatastore(mockEnv);

    assertEquals(roDatastore, rwDatastore);
  }

  @Test
  public void buildDatastoresSuccesSameCredentials() throws Exception {
    factory.setRwUser(new LoginInfo("test", "test"));
    factory.setRoUser(new LoginInfo("test", "test"));

    Datastore rwDatastore = factory.buildReadWriteDatastore(mockEnv);
    Datastore roDatastore = factory.buildReadOnlyDatastore(mockEnv);

    assertEquals(roDatastore, rwDatastore);
  }

  @Test
  public void buildDatastoresSuccessDifferentCredentials() throws Exception {
    factory.setRwUser(new LoginInfo("test", "test"));
    factory.setRoUser(new LoginInfo("", ""));

    Datastore rwDatastore = factory.buildReadWriteDatastore(mockEnv);
    Datastore roDatastore = factory.buildReadOnlyDatastore(mockEnv);

    assertNotEquals(roDatastore, rwDatastore);
  }
}
