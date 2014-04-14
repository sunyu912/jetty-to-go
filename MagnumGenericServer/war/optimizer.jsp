<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Date" %>
<%@ page import="io.magnum.jetty.server.data.*" %>

<html>
<head>
    <title>Magnum ROAR Optimizer</title>
    <script src="/js/jquery.js"></script>
    <script src="/js/roar-control.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.6/angular.min.js"></script>
    <link href="/css/generic.css" rel="stylesheet" type="text/css">
</head>
<body>

  <%               
      Map<String, Integer> apps = (Map<String, Integer>) request.getAttribute("apps");      
  %>

  <div id="appSelector">
	  <table class="block" border="1"> 
	      <caption class="appName">Containers with Benchmark Records </caption>    
	
	      <tr class="headrow">
	          <th>Container ID</th>
	          <th>Records</th>	          
	          <th>Throughput/Sec</th>
	          <th>Latency (ms)</th>
	          <th>Selected</th>
	      </tr>
	      
	      <%  int index = 0;
	          for(Map.Entry<String, Integer> entry : apps.entrySet()) { %>
	      <tr class="datarow">
	          <td class="cell-info-container"><%= entry.getKey() %></td>
	          <td class="cell-info-short"><%= entry.getValue() %></td>
	          <td class="cell-info"><input class="appThroughput" type="text" id="throughput-<%= index %>" name="<%= entry.getKey() %>" value="5000"></td>
	          <td class="cell-info"><input class="appLatency" type="text" id="latency-<%= index %>" name="<%= entry.getKey() %>" value="100"></td>
	          <td class="cell-info-short"><input class="appCheckbox" type="checkbox" name="<%= entry.getKey() %>" value=""></td>
	      </tr>	      
	      <% index++; } %>      
	  </table>
	  
	  <p class="general-text"><input class="optionCheckbox" type="checkbox" name="cotest" value="">Enable Colocation Test</p>
	  <input id="gobutton" type="submit" value="Go" onclick="startOptimizer()">    
  </div>
  
  <div id="resultSection">
  </div>
</body>
</html>