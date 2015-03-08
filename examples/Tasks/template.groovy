<html>
  <head>
    <title>TaskSet Report</title>
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load("visualization", "1", {packages:["table","corechart"]});
      google.setOnLoadCallback(drawCharts);

      function drawCharts() {
        drawUsersTable()
        drawDurationBarChart()
        drawTimingScatterChart()<% ENVIRONMENTS.keySet().each { env-> %>
        draw<% print env.capitalize() %>PieChart()<% } %>
      }

      function drawUsersTable() {
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Name');<% ENVIRONMENTS.keySet().each { env-> %>
        data.addColumn('number', '<% print env.toUpperCase() %> Successes');
        data.addColumn('number', '<% print env.toUpperCase() %> Failures');<% } %>
        data.addColumn('number', 'Total Successes');
        data.addColumn('number', 'Total Failures');
        data.addColumn('number', 'Total SubTasks Performed');
        data.addRows([
          <% USERS.eachWithIndex { it, i->
            name= it.key
            line = "['$name', "
            ENVIRONMENTS.keySet().each {env ->
              pass = USERS[name][env + '_PASS']
              fail = USERS[name][env + '_FAIL']
              line <<= "$pass, $fail, "
            }
            pass = it.value['PASS']
            fail = it.value['FAIL']
            total = it.value['TOTAL']
            line <<= "$pass, $fail, $total"
            line <<= (i < USERS.size() -1 ? '],' : ']')

            print line
          } %>
        ]);
        var table = new google.visualization.Table(document.getElementById('user_table_div'));
        table.draw(data, {showRowNumber: true});
      }

      function drawDurationBarChart() {
        var data = google.visualization.arrayToDataTable([
          ['Duration (seconds)', 'Frequency'],
          <% DURATIONS.eachWithIndex { it, i->
            key = it.key
            val = it.value
            print "['$key', $val]" + (i < DURATIONS.size() -1 ? ',' : '')
          } %>
        ]);
        var options = {
          title: '',
          hAxis: { title: 'TaskSet Duration (in seconds)' },
          vAxis: { title: 'Frequency' },
          legend: { position: 'none' }
        };
        var chart = new google.visualization.ColumnChart(document.getElementById('duration_barchart_div'));
        chart.draw(data, options);
      }

      function drawTimingScatterChart() {
        var data = google.visualization.arrayToDataTable([
          ['Timestamp', 'Total Duration (seconds)', {type: 'string', role: 'tooltip'}, {type: 'string', role: 'style'}],
          <% TIMESTAMPS.eachWithIndex { it, i->
            startTime = it['startTime']
            duration = it['duration']
            numTasks = it['numSubTasks']
            status = it['status']
            color = status == 'PASS' ? 'blue' : 'red'
            tooltip = '(' + startTime.toString() + '): ' + numTasks + ' tasks performed in ' + duration + ' seconds. Status: ' + status
            print "[new Date('$startTime'), $duration, '$tooltip', 'point { fill-color: $color }']" + (i < TIMESTAMPS.size() -1 ? ',' : '')
          } %>
        ]);
        var options = {
          title: '',
          hAxis: { title: 'TaskSet startTime', ticks: [new Date('<% print queryStartTime %>'), new Date('<% print queryEndTime %>')] },
          vAxis: { title: 'TaskSet Duration (in seconds)' },
          legend: { position: 'none' }
        };
        var chart = new google.visualization.ScatterChart(document.getElementById('time_scatterchart_div'));
        chart.draw(data, options);
      }
<% ENVIRONMENTS.each { env, counts-> %>
      function draw<% print env.capitalize() %>PieChart() {
        var data = google.visualization.arrayToDataTable([
          ['Status', 'Frequency'],
          ['PASS', <% print counts['PASS'] %>],
          ['FAIL', <% print counts['FAIL'] %>]
        ]);
        var options = {
          title: 'SubTasks Performed in <% print env.toUpperCase() %>'
        };
        var chart = new google.visualization.PieChart(document.getElementById('<% print env.toLowerCase() %>_piechart'));
        chart.draw(data, options);
      }
<% } %>
    </script>
  </head>
  <body bgcolor="#FFFFFF">
    <h1>TaskSet Report for <% print queryStartTime %> to <% print queryEndTime %></h1>
    A TaskSet is a set of tasks performed in one or more environments (<% print ENVIRONMENTS.keySet().join(', ') %>).
    <br>
    For each environment, the task can be performed if and only if each preceding task in the TaskSet was successful.
    <h1>SubTask Performers</h1>
    <div id="user_table_div" style="width: 850px; height: 150px;"></div>
    <h1>TaskSet Durations</h1>
    <div id="duration_barchart_div" style="width: 600px; height: 300px;"></div>
    <h1>TaskSet Durations Over Time</h1>
    <div id="time_scatterchart_div" style="width: 600px; height: 300px;"></div>
    <h1>SubTask Success/Failure Rates</h1>
    <table>
      <tr>
        <% ENVIRONMENTS.keySet().each { env-> %><td><div id="<% print env.toLowerCase() %>_piechart" style="width: 300px; height: 300px;"></div></td><% } %>
      </tr>
    </table>
  </body>
</html>
