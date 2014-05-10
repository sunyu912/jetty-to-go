<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Map" %>
<%@ page import="io.magnum.jetty.server.data.BatchRunRecord" %>

<html>
<head>
    <title>Batch Run List</title>
    <script src="//code.jquery.com/jquery-1.11.0.min.js"></script>
<script src="//code.jquery.com/jquery-migrate-1.2.1.min.js"></script>

    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.6/angular.min.js"></script>
    <link href="/css/generic.css" rel="stylesheet" type="text/css">        
    <link href="/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/bootstrap-theme.min.css" rel="stylesheet">
    <script type="text/javascript">
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
      List<BatchRunRecord> records = (List<BatchRunRecord>) request.getAttribute("batchRuns");      
  %>  
  
  <table class="table table-striped table-bordered table-hover table-condensed"> 
      <caption class="appName">Batch Run List</caption>
      <thead>
	  <tr class="headrow">
		  <th>ID</th>
		  <th>Description</th>
		  <th>URL List</th>
	  </tr>
	  </thead>

  <%
    for(BatchRunRecord record : records) {
  %>
	  <tr class="datarow">
	  
	      <td class="versionName cell-label">
	          <a href="/v1/bbds/list/batch/history/<%= record.getId() %>"><%= record.getId() %></a>
	      </td>
	  	  
	      <td class="cell-info2">
              <%=record.getDescription() %> 
          </td>
	  	       
          <td class="cell-info">
              <a href="<%= record.getFileUrl() %>" target="_blank"><%= record.getFileUrl() %></a>
          </td>      
      
	  </tr>
  <%
    }
  %>
  </table>
    
  </div>
</body>
</html>