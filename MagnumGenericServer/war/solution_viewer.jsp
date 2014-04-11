<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Date" %>
<%@ page import="io.magnum.jetty.server.data.*" %>

<html>
<head>
    <title>Magnum ROAR Optimizer</title>
    <script src="/js/jquery.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.6/angular.min.js"></script>
    <link href="/css/generic.css" rel="stylesheet" type="text/css">
</head>
<body>

  <%         
      List<ResourceAllocation> individualRAs =  (List<ResourceAllocation>) request.getAttribute("individualSolutions");
      ResourceAllocation resourceAllocation = (ResourceAllocation) request.getAttribute("solution");      
  %>
  <p>
  <table class="block" border="1"> 
      <caption class="appName">Optimized Bin-Packing Solution (Most Cost-Effective) </caption>    

      <tr class="headrow">
          <th>Instance Type</th>
          <th>Instance ID</th>
          <th>Container</th>
          <th>Throughput</th>          
          <th>CPU</th>
          <th>Memory</th>
          <th>Network I/O</th>
          <th>Disk I/O</th>
          <th>Cost</th>
      </tr>
      
      <% for(InstanceResource ir : resourceAllocation.getAllocatedResources()) { %>
      <tr class="datarow">
          <td class="cell-info"><%= ir.getInstanceType() %></td>
          <td class="cell-info-short"><%= ir.getId() %></td>
          <% for(ApplicationAllocation a : ir.getAllocatedApplications()) { %>          
	          <td class="cell-info-container"><%= a.getContainerId() %></td>
	          <td class="cell-info-short"><%= a.getAllocatedThroughput() %></td>
	          <td class="cell-info-short"><%= Math.round(a.getCpu() * 100) / 100 %>%</td>
	          <td class="cell-info-short"><%= Math.round(a.getMem() * 100) / 100 %>%</td>
	          <td class="cell-info-short"><%= Math.round(a.getNetwork() / 1000) %></td>
	          <td class="cell-info-short"><%= a.getDisk() %></td>
          <% } %>
          <td class="cell-info-short"><%= Math.round(ir.getCost() * 10000) / 10000 %></td>              
      </tr>
      <% } %>
      <tr class="datarow">
          <td class="cell-info">TOTAL COST</td>
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info-short"><b><%= Math.round(resourceAllocation.getTotalCost() * 10000) / 10000 %></b></td>              
      </tr>
  </table>
  <p>
  <p>
  <% for(ResourceAllocation ra : individualRAs) { 
        String name = ra.getAllocatedResources().get(0).getAllocatedApplications().get(0).getContainerId(); %>
  <table class="block" border="1"> 
      <caption class="appName">Individual Optimized Bin-Packing Solution: <%= name %></caption>    

      <tr class="headrow">
          <th>Instance Type</th>
          <th>Instance ID</th>
          <th>Container</th>
          <th>Throughput</th>          
          <th>CPU</th>
          <th>Memory</th>
          <th>Network I/O</th>
          <th>Disk I/O</th>
          <th>Cost</th>
      </tr>
      
      <% for(InstanceResource ir : ra.getAllocatedResources()) { %>
      <tr class="datarow">
          <td class="cell-info"><%= ir.getInstanceType() %></td>
          <td class="cell-info-short"><%= ir.getId() %></td>
          <% for(ApplicationAllocation a : ir.getAllocatedApplications()) { %>          
              <td class="cell-info-container"><%= a.getContainerId() %></td>
              <td class="cell-info-short"><%= a.getAllocatedThroughput() %></td>
              <td class="cell-info-short"><%= Math.round(a.getCpu() * 100) / 100 %>%</td>
              <td class="cell-info-short"><%= Math.round(a.getMem() * 100) / 100 %>%</td>
              <td class="cell-info-short"><%= Math.round(a.getNetwork() / 1000) %></td>
              <td class="cell-info-short"><%= a.getDisk() %></td>
          <% } %>
          <td class="cell-info-short"><%= Math.round(ir.getCost() * 10000) / 10000 %></td>              
      </tr>
      <% } %>
      <tr class="datarow">
          <td class="cell-info">TOTAL COST</td>
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info-short"><b><%= Math.round(ra.getTotalCost() * 10000) / 10000 %></b></td>              
      </tr>
  </table>
  <% } %>
  
</body>
</html>