<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Date" %>
<%@ page import="io.magnum.jetty.server.data.*" %>

<html>
<head>
    <title>Magnum ROAR Optimizer</title>
    <script src="/js/jquery.js"></script>
    <script src="/js/thickbox-compressed.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.6/angular.min.js"></script>
    <link href="/css/generic.css" rel="stylesheet" type="text/css">
</head>
<body>

  <%         
      List<ResourceAllocation> individualRAs =  (List<ResourceAllocation>) request.getAttribute("individualSolutions");
      double totalIndividualCost = 0;
      for(ResourceAllocation ra : individualRAs) {
          totalIndividualCost += ra.getTotalCost();
      }
      Integer totalIdealCost = (Integer) request.getAttribute("totalIdealCost");
      Integer totalWorstCost = (Integer) request.getAttribute("totalWorstCost");
      List<List<AppPerformanceRecord>> individualPeaks =  (List<List<AppPerformanceRecord>>) request.getAttribute("individualPeaks");
      List<ResourceAllocation> ras = (List<ResourceAllocation>) request.getAttribute("solutions");      
  %>
  <p>
  <hr>
  
  <table class="block" border="1"> 
      <caption class="appName">Cost Comparison</caption>    
      <tr class="headrow">
          <th>Solution Type</th>
          <th>Total Cost</th>          
      </tr>           
      <%  int index = 0;
          for(ResourceAllocation ra : ras) { index++; %>
      <tr class="datarow">          
          <td class="cell-info-container">BinPacking Solution <%= index %></td>
          <td class="cell-info-short"><%= Math.round(ra.getTotalCost() * 10000) / 10000 %></td>              
      </tr>
      <% } %>
      <tr class="datarow">          
          <td class="cell-info-container">Total Individual Optimized</td>
          <td class="cell-info-short"><%= Math.round(totalIndividualCost * 10000) / 10000 %></td>              
      </tr>
      <tr class="datarow">          
          <td class="cell-info-container">Total Worst Solution</td>
          <td class="cell-info-short"><%= totalWorstCost %></td>              
      </tr>
      <tr class="datarow">          
          <td class="cell-info-container">Total Ideal Most-Optimized</td>
          <td class="cell-info-short"><%= totalIdealCost %></td>              
      </tr>
  </table>
  
  
  <% for(ResourceAllocation resourceAllocation : ras) { %>
  <table class="block" border="1"> 
      <caption class="appName">Optimized Bin-Packing Solution (Most Cost-Effective) </caption>    

      <tr class="headrow">
          <th>Sequence</th>
          <th>Instance Type</th>
          <th>Instance ID</th>
          <th>Container</th>
          <th>Throughput</th>          
          <th>CPU</th>
          <th>Memory</th>
          <th>Network I/O</th>
          <th>Disk I/O</th>
          <th>Co-Test</th>
          <th>Cost</th>
      </tr>
      
      <% for(InstanceResource ir : resourceAllocation.getAllocatedResources()) { %>          
          <% for(ApplicationAllocation a : ir.getAllocatedApplications()) { %>
          <tr class="datarow">
              <td class="cell-info-short"><%= a.getIndex() %></td>
	          <td class="cell-info"><%= ir.getInstanceType() %></td>
	          <td class="cell-info-short"><%= ir.getId() %></td>          
	          <td class="cell-info-container"><%= a.getContainerId() %></td>
	          <td class="cell-info-short"><%= a.getAllocatedThroughput() %></td>
	          <td class="cell-info-short"><%= Math.round(a.getCpu() * 100) / 100 %>%</td>
	          <td class="cell-info-short"><%= Math.round(a.getMem() * 100) / 100 %>%</td>
	          <td class="cell-info-short"><%= Math.round(a.getNetwork() / 1000) %></td>
	          <td class="cell-info-short"><%= a.getDisk() %></td>
	          <% if (ir.getCotestId() != null) { %>
	          <td class="cell-info-short">
	              <a href="/cotest_result_viewer.html?testId=<%= ir.getCotestId() %>" title="ROAR" target="_blank">
                      <img src="/icons/search.png" width="12"></img>
                  </a>
	          </td>
	          <% } else { %>	          
	          <td class="cell-info-short">
                  N/A
              </td>
	          <% } %>
	          
	          <% if (ir.getAllocatedApplications().indexOf(a) == 0) { %>
	              <td class="cell-info-short"><%= Math.round(ir.getCost() * 10000) / 10000 %></td>
	          <% } else { %>
	              <td class="cell-info-short">FREE</td>
	          <% } %>
	              
          <% } %>              
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
          <td class="cell-info"></td>
          <td class="cell-info"></td>
          <td class="cell-info-short"><b><%= Math.round(resourceAllocation.getTotalCost() * 10000) / 10000 %></b></td>              
      </tr>
  </table>
  <% } %>
  <p>
  <p>
  <p>  
  <hr>
  
 <%--  <table class="block" border="1"> 
      <caption class="appName">Cost Comparison</caption>    
      <tr class="headrow">
          <th>Solution Type</th>
          <th>Total Cost</th>          
      </tr>           
      <%  int index = 0;
          for(ResourceAllocation ra : ras) { index++; %>
      <tr class="datarow">          
          <td class="cell-info-container">BinPacking Solution <%= index %></td>
          <td class="cell-info-short"><%= Math.round(ra.getTotalCost() * 10000) / 10000 %></td>              
      </tr>
      <% } %>
      <tr class="datarow">          
          <td class="cell-info-container">Total Individual Optimized</td>
          <td class="cell-info-short"><%= Math.round(totalIndividualCost * 10000) / 10000 %></td>              
      </tr>
      <tr class="datarow">          
          <td class="cell-info-container">Total Worst Solution</td>
          <td class="cell-info-short"><%= totalWorstCost %></td>              
      </tr>
      <tr class="datarow">          
          <td class="cell-info-container">Total Ideal Most-Optimized</td>
          <td class="cell-info-short"><%= totalIdealCost %></td>              
      </tr>
  </table> --%>
   
  <p>
  <hr>
          
  <% for(int i = 0; i < individualRAs.size(); i++) { 
        String name = individualRAs.get(i).getAllocatedResources().get(0).getAllocatedApplications().get(0).getContainerId(); %>
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
      
      <% for(InstanceResource ir : individualRAs.get(i).getAllocatedResources()) { %>
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
          <td class="cell-info-short"><b><%= Math.round(individualRAs.get(i).getTotalCost() * 10000) / 10000 %></b></td>              
      </tr>
  </table>
  
  <table class="block2" border="1"> 
      <caption class="appName">Peak Performance</caption>    

      <tr class="headrow2">
          <th>Instance Type</th>
          <th>Peak Throughput</th>
          <th>Latency</th>
          <th>Cost/Throughput</th>
          <th>CPU</th>
          <th>Memory</th>
          <th>Network I/O</th>
          <th>Disk I/O</th>
      </tr>
      
      <% for(AppPerformanceRecord r : individualPeaks.get(i)) { %>
      <tr class="datarow">
          <td class="cell-info"><%= r.getInstanceType() %></td>
          <% if (r.getPeakRecord() != null) { %>
	          <td class="cell-info-short"><%= r.getPeakRecord().getThroughput() %></td>
	          <td class="cell-info-short"><%= r.getPeakRecord().getLatency() %></td>
	          <td class="cell-info-short"><%= r.getCostAtPeak() %></td>
	          <td class="cell-info-short"><%= Math.round(r.getPeakRecord().getCpu() * 100) / 100 %>%</td>
	          <td class="cell-info-short"><%= Math.round(r.getPeakRecord().getMem() * 100) / 100 %>%</td>
	          <td class="cell-info-short"><%= Math.round(r.getPeakRecord().getNetwork() / 1000) %></td>
	          <td class="cell-info-short"><%= r.getPeakRecord().getDisk() %></td>
          <% } else { %>
              <td class="cell-info-short"> N/A </td>
          <% } %>    
      </tr>
      <% } %>
  </table>
  <p><p>
  <% } %>
  
</body>
</html>