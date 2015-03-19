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

package com.callidusrobotics.droptables.view;

import io.dropwizard.views.View;

import com.callidusrobotics.droptables.model.ReportGenerator;

/**
 * View class for executing ReportGenerator objects.
 *
 * @author Rusty Gerard
 * @since 0.0.1
 * @see ReportGenerator
 */
public class ReportExecuteView extends View {
  private final ReportGenerator report;

  public ReportExecuteView(ReportGenerator report) {
    super("report_execute.ftl");
    this.report = report;
  }

  public ReportGenerator getReport() {
    return report;
  }
}
