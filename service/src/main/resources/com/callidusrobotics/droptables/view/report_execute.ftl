<#-- @ftlvariable name="" type="com.callidusrobotics.droptables.model.ReportGenerator" -->
<html>
  <head>
    <title>Execute Report Generator</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
    <script language="javascript">
      function doPost() {
        var payload = new Object();
        <#list report.binding?keys as key>
        payload["${key}"] = $("#param\\[${key}\\]").val();
        </#list>

        console.log("POSTing: " + JSON.stringify(payload));
        var request = $.ajax({
          url: "/reports/${report.id}/results",
          type: "POST",
          data: JSON.stringify(payload),
          contentType: "application/json",
          dataType: "json"
        });

        request.always( function(data) {
          var newDoc = document.open("text/html", "replace");
          newDoc.write(data.responseText);
          newDoc.close();
        } );
      }
    </script>
  </head>
  <body>
    <b>ID:</b> <input type="text" size="30" id="id" value="${report.id}" readonly="true" /><br>
    <b>Date Created:</b> <input type="text" size="30" id="created" value="${report.created?string("yyyy-MM-dd HH:mm:ss")}" readonly="true" /><br>
    <b>Date Modified:</b> <input type="text" size="30" id="modified" value="${report.modified?string("yyyy-MM-dd HH:mm:ss")}" readonly="true" /><br>
    <b>Name:</b> <input type="text" size="30" id="name" value="${report.name}" readonly="true" /><br>
    <b>Author:</b> <input type="text" size="30" id="author" value="${report.author}" readonly="true" /><br>
    <b>Language:</b> <input type="text" size="30" id="language" value="${report.language}" readonly="true" /><br><br>
    <b>Description:</b><br>
    <textarea rows="5" cols="80" id="description" readonly="true">${report.description}</textarea></p>
    <p><b>Parameters:</b><br>
    <#list report.binding?keys as key>
    <b>${key}:</b>
    <input type="text" size="15" id="param[${key}]" value="${report.binding?values[key_index]}" /><br>
    </#list>
    </p>
    <p>
    <button onclick="doPost()">Execute</button>
    </p>
  </body>
</html>
