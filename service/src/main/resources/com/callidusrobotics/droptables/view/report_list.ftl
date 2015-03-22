<#-- @ftlvariable name="" type="com.callidusrobotics.droptables.model.ReportGenerator" -->
<!DOCTYPE html> 
<html>
  <head>
    <#include "head.ftl">
  </head>
  <body>
    <#include "navbar.ftl">
    
    <h1><a href="/reports/new">Create New Report</a></h1><br>
    <#if reports?has_content>
    <table>
      <tr>
        <td><b>NAME</b></td>
        <td><b>AUTHOR</b></td>
        <td><b>LANGUAGE</b></td>
        <td colspan="3"><b>DESCRIPTION</b></td>
      </tr>
      <#list reports as report>
      <tr>
        <td>${report.name}</td>
        <td>${report.author}</td>
        <td>${report.language}</td>
        <td>${report.description}</td>
        <td><a href="/reports/${report.id}">Edit</a></td>
        <td><a href="/reports/${report.id}/results">Execute</td>
      </tr>
      </#list>
    </table>
    </#if>
  </body>
</html>
