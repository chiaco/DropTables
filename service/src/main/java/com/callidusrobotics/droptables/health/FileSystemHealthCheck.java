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

import java.io.File;

import com.callidusrobotics.droptables.configuration.DropTablesConfig;
import com.codahale.metrics.health.HealthCheck;

public class FileSystemHealthCheck extends HealthCheck {
  private final String scriptsCacheDir;

  public FileSystemHealthCheck(DropTablesConfig config, Environment environment) {
    scriptsCacheDir = config.getScriptsCacheDir();
  }

  @Override
  protected Result check() throws Exception {
    try {
      if (new File(scriptsCacheDir).canWrite()) {
        return Result.healthy();
      }
    } catch (SecurityException e) {
      return Result.unhealthy(e.getMessage());
    }

    return Result.unhealthy("Permission denied.");
  }
}
