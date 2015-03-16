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

package com.callidusrobotics.droptables.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.Script;
import groovy.text.Template;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.test.JSONAssert;

import org.bson.types.ObjectId;
import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.Before;
import org.junit.Test;

import com.callidusrobotics.droptables.model.ReportGenerator.Language;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReportGeneratorTest {
  static final String GOOD_SCRIPT = "var1 = [0, 1, 2]\nprint var1";
  static final String BAD_SCRIPT = "var1 = [\nprint var1";
  static final String GOOD_TEMPLATE = "<html><% print [0, 1, 2] %></html>";
  static final String BAD_TEMPLATE = "<html><% print [0, ";

  ReportGenerator reportGenerator;

  @Before
  public void before() {
    reportGenerator = new ReportGenerator();
  }

  @Test
  public void toJsonSuccess() throws Exception {
    reportGenerator.created = new Date(1111111);
    reportGenerator.modified = new Date(5555555);
    reportGenerator.id = new ObjectId("54e8f1dbd9a93c9b467d5380");
    reportGenerator.language = Language.GROOVY;
    reportGenerator.script = GOOD_SCRIPT;
    reportGenerator.template = GOOD_TEMPLATE;
    reportGenerator.name = "Script1";
    reportGenerator.author = "John Doe";
    reportGenerator.description = "This is a script for testing serialization";

    String expectedJson = "{\"id\":{\"date\":1424552411000,\"time\":1424552411000,\"timestamp\":1424552411,\"timeSecond\":1424552411,\"inc\":1182618496,\"machine\":-643220325,\"new\":false},\"dateCreated\":1111111,\"dateModified\":5555555,\"name\":\"Script1\",\"description\":\"This is a script for testing serialization\",\"author\":\"John Doe\",\"language\":\"GROOVY\",\"template\":\"<html><% print [0, 1, 2] %></html>\",\"script\":\"var1 = [0, 1, 2]\\nprint var1\",\"defaultParameters\":{}}";

    // Unit under test
    ObjectMapper mapper = new ObjectMapper();
    String result = mapper.writeValueAsString(reportGenerator);

    // Verify results
    JSONAssert.assertJsonEquals(expectedJson, result);
  }

  @Test
  public void prePersistNewObject() throws Exception {
    reportGenerator.created = reportGenerator.modified = null;

    // Unit under test
    reportGenerator.prePersist();

    // Verify results
    assertNotNull("dateCreated is null", reportGenerator.getCreated());
    assertNotNull("dateModified is null", reportGenerator.getModified());
    assertEquals(reportGenerator.getCreated(), reportGenerator.getModified());
  }

  @Test
  public void prePersistModifiedObject() throws Exception {
    Date dateInitial = new Date(0);
    reportGenerator.created = reportGenerator.modified = dateInitial;

    // Unit under test
    reportGenerator.prePersist();

    // Verify results
    assertNotNull("dateCreated is null", reportGenerator.getCreated());
    assertEquals("dateCreated was modified", dateInitial, reportGenerator.getCreated());
    assertNotNull("dateModified is null", reportGenerator.getModified());
    assertTrue("dateModified was not updated", dateInitial.getTime() != reportGenerator.getModified().getTime());
  }

  @Test(expected = NullPointerException.class)
  public void setLanguageFailureNull() throws Exception {
    reportGenerator.setLanguage(null);
  }

  @Test
  public void setScriptSuccess() throws Exception {
    reportGenerator.setScript(GOOD_SCRIPT);

    assertEquals(GOOD_SCRIPT, reportGenerator.getScript());
  }

  @Test(expected = CompilationFailedException.class)
  public void setScriptFailure() throws Exception {
    reportGenerator.setScript(BAD_SCRIPT);
  }

  @Test
  public void parseScriptSuccess() throws Exception {
    reportGenerator.script = GOOD_SCRIPT;
    Script result = reportGenerator.parseScript();

    assertNotNull("Failed to parse script", result);
  }

  @Test(expected = GroovyRuntimeException.class)
  public void parseScriptFailure() throws Exception {
    reportGenerator.script = BAD_SCRIPT;
    reportGenerator.parseScript();
  }

  @Test
  public void setTemplateSuccess() throws Exception {
    reportGenerator.setTemplate(GOOD_TEMPLATE);

    assertEquals(GOOD_TEMPLATE, reportGenerator.getTemplate());
  }

  @Test(expected = GroovyRuntimeException.class)
  public void setTemplateFailure() throws Exception {
    reportGenerator.setTemplate(BAD_TEMPLATE);
  }

  @Test
  public void parseTemplateSuccess() throws Exception {
    reportGenerator.template = GOOD_TEMPLATE;
    Template result = reportGenerator.parseTemplate();

    assertNotNull("Failed to parse template", result);
  }

  @Test(expected = GroovyRuntimeException.class)
  public void parseTemplateFailure() throws Exception {
    reportGenerator.template = BAD_TEMPLATE;
    reportGenerator.parseTemplate();
  }

  @Test
  public void parseBindingsSuccessNoBindings() throws Exception {
    Binding result = reportGenerator.parseBinding();

    assertNotNull("Failed to parse variable bindings", result);
  }

  @Test
  public void parseBindingsSuccess() throws Exception {
    Map<String, String> bindings = new HashMap<String, String>();
    bindings.put("a", "1");
    bindings.put("b", "2");
    bindings.put("c", "3");

    // Unit under test
    reportGenerator.setBinding(bindings);
    Binding result = reportGenerator.parseBinding();

    // Verify results
    assertNotNull("Failed to parse variable bindings", result);
    for (String key : bindings.keySet()) {
      assertTrue("Binding is missing variable \"" + key + "\"", result.hasVariable(key));
      assertEquals("Binding value is wrong for \"" + key + "\"", (String) bindings.get(key), (String) result.getVariable(key));
    }
  }
}
