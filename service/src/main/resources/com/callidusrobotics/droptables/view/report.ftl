<#-- @ftlvariable name="" type="com.callidusrobotics.droptables.model.ReportGenerator" -->
<html>
  <head>
    <title>View Report Generator</title>
  </head>
  <body>
    <b>ID:</b> <input type="text" size="30" name="id" value="${report.id}" readonly="true" /><br>
    <b>Date Created:</b> <input type="text" size="30" name="created" value="${report.created?string("yyyy-MM-dd HH:mm:ss")}" readonly="true" /><br>
    <b>Date Modified:</b> <input type="text" size="30" name="modified" value="${report.modified?string("yyyy-MM-dd HH:mm:ss")}" readonly="true" /><br>
    <b>Name:</b> <input type="text" size="30" name="name" value="${report.name}" /><br>
    <b>Author:</b> <input type="text" size="30" name="author" value="${report.author}" /><br>
    <b>Language:</b> <select name="languages"><#list getLanguages() as language><option value="${language}">${language}</option></#list></select><br>
    <b>Description:</b><br>
    <textarea rows="5" cols="80" name="description">${report.description}</textarea></p><br>
    <p><b>Template:</b><br>
    <textarea rows="50" cols="80" name="template">${report.template}</textarea></p><br>
    <p><b>Script:</b><br>
    <textarea rows="50" cols="80" name="script">${report.script}</textarea></p><br>
    <p><b>Default Parameters:</b><br><#list report.binding?keys as key>
    <b>Key:</b> <input type="text" size="15" name="keys[${key_index}]" value="${key}" />
    <b>Value:</b> <input type="text" size="15" name="vals[${key_index}]" value="${report.binding?values[key_index]}" /><br></#list></p>
  </body>
</html>
