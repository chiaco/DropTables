<#-- @ftlvariable name="" type="com.callidusrobotics.droptables.model.ReportGenerator" -->
<!DOCTYPE html> 
<html>
  <head>
    <#include "head.ftl">
  </head>
  <body>
    <div class="wrapper">
    <header>
       <div class="headerTitle"><h1>DropTables</h1>Dropping tomorrow's reports, today!</div>
       <#include "navbar.ftl">
    </header>
    
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
    </div>


    <br><br><br><br>
    
    
    
    <div class="navigation">
  		<ul class="nav">
  			<li>
  				<a href="#">Home</a>
  			</li>
  			<li>
  				<a href="#">Services</a>
  				<ul>
  					<li><a href="#">Consulting</a></li>
  					<li><a href="#">Sales</a></li>
  					<li><a href="#">Support</a></li>
  				</ul>
  			</li>
  			<li>
  				<a href="#">About Us</a>
  				<ul>
  					<li><a href="#">Company</a></li>
  					<li><a href="#">Mission</a></li>
  					<li><a href="#">Contact Information</a></li>
  				</ul>
  			</li>
  		</ul>
  	</div>
  	
  	
  	
  	
  	<br><br><br><br>
  	
  	
    <footer>
      <#include "footer.ftl">
    </footer>
  </body>
</html>
