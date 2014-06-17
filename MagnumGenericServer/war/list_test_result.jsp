<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Date" %>
<%@ page import="io.magnum.jetty.server.data.BenchmarkRecord" %>

<html>
<head>
    <title>Magnum Test Result Checker</title>
    <script src="/js/jquery.js"></script>
    <script src="/js/thickbox-compressed.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.6/angular.min.js"></script>
    <script type="text/javascript">
	    function showResultPageInIFrame(url) {
	    	$('#resultIFrame').attr('src', url);	
	    }	    
    </script>
    <style type="text/css" media="all">@import "/css/thickbox.css";</style>
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
		font-size: 12;
		background-color: #EEE;
		text-align: left;
		width: 200;
		}
		td.cell-info {
		font-family: Arial;
		font-size: 12;
		background-color: #EEE;
		width: 250;
		}
		td.cell-info-short {
        font-family: Arial;
        font-size: 12;
        background-color: #EEE;
        width: 110;
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
        
        #resultIFrame {
            width: 900;
            height: 2000;
        }
    </style>

</head>
<body>

  <% 
      List<BenchmarkRecord> records =  (List<BenchmarkRecord>) request.getAttribute("benchmarkRecords");
      String title = (String) request.getAttribute("title");
  %>  
  
  <table class="block" border="1"> 
      <caption class="appName">Benchmark Records for <%=title %></caption>	 

      <tr class="headrow">
          <th>ContainerId</th>
          <th>Timestamp</th>
          <th>Instance Type</th>
          <th>Test Id</th>
          <th>Viewer</th>
          <th>Notes</th>
      </tr>
      
      <% for(BenchmarkRecord record : records) { Date date = new Date(record.getTimestamp() * 1000); %>
	  <tr class="datarow">
	      <td class="cell-label"> <%= record.getContainerId() %> </td>	 	  
	      <td class="cell-info"><%= record.getTimestamp() %> (<%= date.toGMTString() %>)</td>	  	                     
          <td class="cell-info-short"><%= record.getInstanceType() %></td>                    
          <td class="cell-info"><a href="/v1/roar/test/run/<%=record.getTestId() %>/checker"><%= record.getTestId() %></a></td>
          <%-- <td class="cell-info-short"><a href="javascript:showResultPageInIFrame('/result_viewer.html?testId=<%= record.getTestId() %>&app=<%=record.getContainerId() %>&type=<%=record.getInstanceType() %>');">Open Viewer</a></td> --%>
          <td class="cell-info-short">
            <% if (record.isMtRecord()) { %>
              <a href="/result_viewer3.html?testId=<%= record.getTestId() %>&id=<%= record.getContainerId() %>&timestamp=<%= record.getTimestamp()%>&app=<%=record.getContainerId() %>&type=<%=record.getInstanceType() %>&KeepThis=true&TB_iframe=true&height=800&width=900" title="ROAR" class="thickbox">Open Viewer</a>
            <% } else { %>
              <a href="/result_viewer.html?testId=<%= record.getTestId() %>&app=<%=record.getContainerId() %>&type=<%=record.getInstanceType() %>&KeepThis=true&TB_iframe=true&height=800&width=900" title="ROAR" class="thickbox">Open Viewer</a>
            <% } %>
          </td>
          
          <td class="cell-info">
            <% if (!record.isMtRecord()) { %>
                <%= record.getNotes() %>
            <% } %>
          </td>  
      </tr>
      <% } %>
  </table>
  
</body>
</html>