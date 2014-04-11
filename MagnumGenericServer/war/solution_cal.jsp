<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Date" %>
<%@ page import="io.magnum.jetty.server.data.*" %>

<html>
<head>
    <title>Magnum ROAR Optimizer</title>
    <script src="/js/jquery.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.6/angular.min.js"></script>
    <style type="text/css">
    
        table.block { 
		border-width: 3px;
		border-style: solid;
		border-collapse: collapse;
		border-color: #A6CDDE;
		}
		table.error {
		border-width: 3px;
		border-style: solid;
		border-collapse: collapse;
		border-color: #F6358A;
		}
		tr.headrow {
		font-family: Arial;
		font-size: 14;
		font-weight: bold;
		background-color: #A6CDDE;
		width: 650; 
		}
		tr.error {
		font-family: Arial;
		font-size: 14;
		font-weight: bold;
		background-color: #F6358A;
		width: 530;
		}
		tr.datarow {
		border-width: 2px;
		border-style: solid;
		border-color: #FFFFFF;
		}
		td.cell-label {
		font-family: Arial;
		font-weight: bold;
		font-size: 12;
		background-color: #EEE;
		text-align: left;
		width: 120;
		}
		td.cell-info {
		font-family: Arial;
		font-size: 12;
		background-color: #EEE;
		width: 580;
		}

        .appName {
            font-family: Arial;
            font-weight:bold;
            font-size: 14;
        }
        
        .notes {
            font-size: 12;
            font-weight:bold;
            font-family: Arial;
        }        
        
        .versionName {           
            text-align:center;
            width: 200px;
        }
        
        .currentVer {           
            color: yellow;
            background-color: green;
            text-align:center;
            width: 180px;
        }
        
        .envButton {            
            text-align:center;
            width: 180px;
        }       
    </style>

</head>
<body>

  <%         
      List<AppPerformanceRecord> records =  (List<AppPerformanceRecord>) request.getAttribute("peakResultList");
      ResourceAllocation resourceAllocation = (ResourceAllocation) request.getAttribute("solution");
      Integer throughput = (Integer) request.getAttribute("throughput");
      Double latency = (Double) request.getAttribute("latency");
  %>
    
  <table class="block" border="1"> 
      <caption class="appName">Peak Performance Comparison </caption>	 

      <tr class="headrow">
          <th>Instance Type</th>
          <th>Peak Throughput</th>
          <th>Latency</th>
          <th>Cost/Throughput</th>
          <th>CPU</th>
          <th>Memory</th>
          <th>Network I/O</th>
          <th>Disk I/O</th>
      </tr>
      
      <% for(AppPerformanceRecord r : records) { %>
      <tr class="datarow">     
          <td class="cell-info"><%= r.getInstanceType() %></td>
          <td class="cell-info"><%= r.getPeakRecord().getThroughput() %></td>
          <td class="cell-info"><%= r.getPeakRecord().getLatency() %></td>
          <td class="cell-info"><%= r.getCostAtPeak() %></td>
          <td class="cell-info"><%= Math.round(r.getPeakRecord().getCpu() * 100) / 100 %>%</td>
          <td class="cell-info"><%= Math.round(r.getPeakRecord().getMem() * 100) / 100 %>%</td>
          <td class="cell-info"><%= Math.round(r.getPeakRecord().getNetwork() / 1000) %></td>
          <td class="cell-info"><%= r.getPeakRecord().getDisk() %></td>    
      </tr>
      <% } %>
  </table>
  
  <% if (throughput != null) { %>
  
  <p>
  <table class="block" border="1"> 
      <caption class="appName">Given Throughput/Latency Performance Comparison </caption>    

      <tr class="headrow">
          <th>Instance Type</th>
          <th>Expected (Peak)</th>
          <th>Latency</th>
          <th>Cost/Throughput</th>
          <th>CPU</th>
          <th>Memory</th>
          <th>Network I/O</th>
          <th>Disk I/O</th>
      </tr>
      
      <% for(AppPerformanceRecord r : records) { %>
      <tr class="datarow">     
          <td class="cell-info"><%= r.getInstanceType() %></td>
          <td class="cell-info"><%= r.getGivenThroughput() %> (<%= r.getPeakRecord().getThroughput() %>)</td>
          <td class="cell-info"><%= r.getGivenThroughputRecord().getLatency() %></td>
          <td class="cell-info"><%= r.getCostAtGivenThroughput() %></td>
          <td class="cell-info"><%= Math.round(r.getGivenThroughputRecord().getCpu() * 100) / 100 %>%</td>
          <td class="cell-info"><%= Math.round(r.getGivenThroughputRecord().getMem() * 100) / 100 %>%</td>
          <td class="cell-info"><%= Math.round(r.getGivenThroughputRecord().getNetwork() / 1000) %></td>
          <td class="cell-info"><%= r.getGivenThroughputRecord().getDisk() %></td>    
      </tr>
      <% } %>
  </table>
  
  <p>
  <table class="block" border="1"> 
      <caption class="appName">Optimized Solution (Most Cost-Effective) </caption>    

      <tr class="headrow">
          <th>Instance Type</th>
          <th>Instance ID</th>
          <th>Application</th>
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
          <td class="cell-info"><%= ir.getId() %></td>
          <% for(ApplicationAllocation a : ir.getAllocatedApplications()) { %>
	          <td class="cell-info"><%= a.getContainerId() %></td>
	          <td class="cell-info"><%= a.getAllocatedThroughput() %></td>
	          <td class="cell-info"><%= Math.round(a.getCpu() * 100) / 100 %>%</td>
	          <td class="cell-info"><%= Math.round(a.getMem() * 100) / 100 %>%</td>
	          <td class="cell-info"><%= Math.round(a.getNetwork() / 1000) %></td>
	          <td class="cell-info"><%= a.getDisk() %></td>
          <% } %>
          <td class="cell-info"><%= Math.round(ir.getCost() * 10000) / 10000 %></td>              
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
          <td class="cell-info"><b><%= Math.round(resourceAllocation.getTotalCost() * 10000) / 10000 %></b></td>              
      </tr>
  </table>
  
  <% } %>
  
</body>
</html>