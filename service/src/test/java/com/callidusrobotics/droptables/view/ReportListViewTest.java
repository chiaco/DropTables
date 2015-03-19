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

import static org.junit.Assert.assertTrue;
import io.dropwizard.views.freemarker.FreemarkerViewRenderer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.callidusrobotics.droptables.model.ReportGenerator;
import com.callidusrobotics.droptables.model.ReportGeneratorTest;

@RunWith(MockitoJUnitRunner.class)
public class ReportListViewTest {
  ReportListView view;
  List<ReportGenerator> reports;

  FreemarkerViewRenderer renderer;
  OutputStream writer;

  @Before
  public void before() throws Exception {
    reports = new ArrayList<ReportGenerator>();
    for (int i=0; i<10; i++) {
      ReportGenerator report = ReportGeneratorTest.buildReport();
      report.setName("Doc " + i);
      reports.add(report);
    }

    view = new ReportListView(reports);
    renderer = new FreemarkerViewRenderer();
    writer = new ByteArrayOutputStream();
  }

  @Test
  public void renderFreeMarkerSuccess() throws Exception {
    // Unit under test
    renderer.render(view, Locale.ENGLISH, writer);

    // Verify results
    String result = writer.toString();

    for (int i=0; i<reports.size(); i++) {
      ReportGenerator report = reports.get(i);

      assertTrue("ID field was not set for #" + i, result.contains(report.getId().toString()));
      assertTrue("Name field was not set for #" + i, result.contains(report.getName()));
      assertTrue("Author field was not set for #" + i, result.contains(report.getAuthor()));
      assertTrue("Description field was not set for #" + i, result.contains(report.getDescription()));
      assertTrue("Language field was not set for #" + i, result.contains(report.getLanguage().toString()));
    }
  }
}
