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

package com.callidusrobotics.droptables.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * HTTP error response that produces an HTML error message.
 *
 * @author Rusty Gerard
 * @since 0.0.1
 */
public class HtmlWebApplicationException extends WebApplicationException {
  private static final long serialVersionUID = -7334525235445194321L;

  public HtmlWebApplicationException(Throwable cause, Response.Status status) {
    super(cause, Response.status(status).type(MediaType.TEXT_HTML).entity(getMessage(cause, status.getStatusCode())).build());
  }

  protected static String getMessage(Throwable cause, int statusCode) {
    StringBuilder builder = new StringBuilder();
    builder.append("<html><head><title>");
    builder.append(statusCode);
    builder.append(" Error</title></head><body><pre>");
    builder.append(ExceptionUtils.getStackTrace(cause));
    builder.append("</pre></body></html>");

    return builder.toString();
  }
}
