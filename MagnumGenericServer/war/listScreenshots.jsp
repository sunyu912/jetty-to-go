<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Map" %>
<%@ page import="io.magnum.jetty.server.data.ScreenshotRecord" %>

<html>
<head>
    <title>Optio App Version Selector</title>
    <script src="/js/jquery-1.9.1.min.js"></script>
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
		width: 200;
		}
		td.cell-info {
		font-family: Arial;
		font-size: 12;
		background-color: #EEE;
		width: 270;
		}
		td.cell-info2 {
        font-family: Arial;
        font-size: 12;
        background-color: #EEE;
        width: 500;
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
            text-align:left;
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
      List<ScreenshotRecord> records = (List<ScreenshotRecord>) request.getAttribute("records");      
  %>  
  
  <table class="block" border="1"> 
      <caption class="appName">Screenshots History</caption>
	  <tr class="headrow">
		  <th>URL</th>
		  <th>Timestamp</th>
		  <th>Screenshot URL</th>
	  </tr>

  <%
    for(ScreenshotRecord record : records) {
  %>
	  <tr class="datarow">
	  
	      <td class="versionName cell-label">
	          <a href="<%= record.getUrl() %>"><%= record.getUrl() %></a>
	      </td>
	  	  
	  	  <%
	  	      String dateStr = new Date(record.getTimestamp()).toGMTString();
	  	  %>
	      <td class="envButton cell-info">
              <%=record.getTimestamp() %> (<%=dateStr %>) 
          </td>
	  	       
          <td class="envButton cell-info2">
              <a href="<%= record.getImageS3Url() %>" target="_blank"><%= record.getImageS3Url() %></a>
          </td>      
                  
	  </tr>
  <%
    }
  %>
  </table>
  
</body>
</html>