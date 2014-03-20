<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Date" %>
<%@ page import="io.magnum.jetty.server.data.TestInfo" %>

<html>
<head>
    <title>Magnum Test Result Checker</title>
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
      TestInfo testInfo =  (TestInfo) request.getAttribute("testInfo");
      String testId = testInfo.getId();
      Long timestamp = testInfo.getTimestamp();
      Date date = new Date(timestamp);
      String status = testInfo.getStatus();
      String modelUrl = testInfo.getResultModelUrl();
      String recordsUrl = testInfo.getTestRecordsUrl();
      String logUrl = testInfo.getJMeterLogUrl();            
  %>
    
  <table class="block" border="1"> 
      <caption class="appName">Load Test: <%=testId %></caption>	 

	  <tr class="datarow">
	      <td class="versionName cell-label"> TestId </td>	 	  
	      <td class="cell-info"><%= testId %></td>	  
	  </tr>
	  <tr class="datarow">
          <td class="versionName cell-label"> Timestamp </td>         
          <td class="cell-info"><%= timestamp %> (<%= date.toGMTString() %>)</td>    
      </tr>
      <tr class="datarow">
          <td class="versionName cell-label"> Status </td>         
          <td class="cell-info"><%= status %></td>    
      </tr>
      <tr class="datarow">
          <td class="versionName cell-label"> Result in Viewer </td>
          <% if (modelUrl != null)  { %>            
          <td class="cell-info"><a href="/result_viewer.html?testId=<%= testId %>">Open Viewer</a></td>
          <% } else { %>
          <td class="cell-info">N/A</td>
          <% } %>    
      </tr>
      <tr class="datarow">
          <td class="versionName cell-label"> Result Data URL </td>      
          <% if (modelUrl != null)  { %>   
          <td class="cell-info"><a href="<%= modelUrl %>"><%= modelUrl %></a></td>
          <% } else { %>
          <td class="cell-info">N/A</td>
          <% } %>    
      </tr>
      <tr class="datarow">
          <td class="versionName cell-label"> Records Data URL </td>
          <% if (recordsUrl != null)  { %>         
          <td class="cell-info"><a href="<%= recordsUrl %>"><%= recordsUrl %></a></td>
          <% } else { %>
          <td class="cell-info">N/A</td>
          <% } %>    
      </tr>
      <tr class="datarow">
          <td class="versionName cell-label"> JMeter Log URL </td>  
          <% if (logUrl != null)  { %>       
          <td class="cell-info"><a href="<%= logUrl %>"><%= logUrl %></a></td>
          <% } else { %>
          <td class="cell-info">N/A</td>
          <% } %>    
      </tr>
  </table>
  
</body>
</html>