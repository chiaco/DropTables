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

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginInfo {
  @NotNull
  private String username;

  @NotNull
  private String password;

  public LoginInfo() {
    username = password = "";
  }

  public LoginInfo(String username, String password) {
    setUsername(username);
    setPassword(password);
  }

  @JsonProperty
  public String getUsername() {
    return username;
  }

  @JsonProperty
  public void setUsername(String username) {
    this.username = StringUtils.trimToEmpty(username);
  }

  @JsonProperty
  public String getPassword() {
    return password;
  }

  @JsonProperty
  public void setPassword(String password) {
    this.password = StringUtils.trimToEmpty(password);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof LoginInfo)) {
      return false;
    }

    LoginInfo otherInfo = (LoginInfo) other;
    return StringUtils.equals(username + " " + password, otherInfo.username + " " + otherInfo.password);
  }

  @Override
  public int hashCode() {
    return (username + " " + password).hashCode();
  }
}
