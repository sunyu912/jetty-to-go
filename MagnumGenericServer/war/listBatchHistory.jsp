<%@ page import="java.util.*" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Map" %>
<%@ page import="io.magnum.jetty.server.data.BatchRunHistoryRecord" %>

<html>
<head>
    <title>Batch Run List</title>
    <script src="/js/jquery.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.6/angular.min.js"></script>
    <link href="/css/generic.css" rel="stylesheet" type="text/css">    
    <link href="/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/bootstrap-theme.min.css" rel="stylesheet">    
    <script type="text/javascript">
        function startBatchRun(batchId) {
        	$.ajax(
        		    {
        		        type : "GET",
        		        url  : "/v1/bbds/batch/run",
        		        data : {
        		            id : batchId
        		        },
        		        success : function(result) {                   
        		        	location.reload();
        		        },
        		        error: function (jqXHR, exception) {
        		            console.log("Faiiled to load result solution.");
        		        }
        		    });
        }
    </script>
</head>
<body>

  <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="container">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="/index.html">AdsMei</a>
      </div>
      <div class="navbar-collapse collapse">
        <ul class="nav navbar-nav">
          <li><a href="/index.html">Home</a></li>
          <li class="active"><a href="/v1/bbds/list/batch">Batch</a></li>
          <li><a href="#ranking">Ranking</a></li>            
        </ul>
      </div><!--/.nav-collapse -->
    </div>
  </div>

  <div class="container theme-showcase main-page-container" role="main">
  <% 
      List<BatchRunHistoryRecord> records = (List<BatchRunHistoryRecord>) request.getAttribute("batchHistory");
      if (records == null) records = new ArrayList<BatchRunHistoryRecord>();
      String batchId = (String) request.getAttribute("id");
  %>  
  
  <table class="block" border="1"> 
      <caption class="appName">Batch Run List</caption>
	  <tr class="headrow">
		  <th>ID</th>
		  <th>Timestamp</th>
		  <th>Completed</th>
	  </tr>

  <%
    for(BatchRunHistoryRecord record : records) {
  %>
	  <tr class="datarow">
	  
	      <td class="versionName cell-label">
	          <%= record.getId() %>
	      </td>
	  	  
	      <td class="cell-info2">
	          <a href="/v1/bbds/list/batch/run/result/<%= record.getTimestamp() %>"><%=record.getTimestamp() %></a>              
          </td>
	  	       
          <td class="cell-info-short">
              <%=record.isCompleted() %>
          </td>      
                  
	  </tr>
  <%
    }
  %>
  </table>
  
  <input id="gobutton" type="submit" value="Run" onclick="startBatchRun('<%= batchId %>')">    
  </div>
</body>
</html>