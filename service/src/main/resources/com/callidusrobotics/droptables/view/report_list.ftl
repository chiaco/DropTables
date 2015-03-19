<#-- @ftlvariable name="" type="com.callidusrobotics.droptables.model.ReportGenerator" -->
<html>
  <head>
    <title>Report Generators</title>
    <style>
      table {
        width: 100%;
      }

      table, th, td {
        border: 1px solid black;
        border-collapse: collapse;
      }
    </style>
  </head>
  <body>
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
