<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Map" %>
<%@ page import="io.magnum.jetty.server.data.BatchRunResultRecord" %>

<html>
<head>
    <title>Optio App Version Selector</title>
    <script src="/js/jquery.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.6/angular.min.js"></script>
    <link href="/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/bootstrap-theme.min.css" rel="stylesheet">
    <link href="/css/generic.css" rel="stylesheet" type="text/css">        
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
      List<BatchRunResultRecord> records = (List<BatchRunResultRecord>) request.getAttribute("batchRunResult");            
  %>  
  
  <table class="block" border="1" > 
      <caption class="appName">Screenshots History</caption>
	  <tr class="headrow">
		  <th>Timestamp</th>
		  <th>Index</th>
		  <th>URL</th>
		  <th>Screenshot URL</th>
		  <th>Color Result</th>
		  <th>XY Result</th>
		  <th>Quad Result</th>
	  </tr>

  <%
    for(BatchRunResultRecord record : records) {
  %>
	  <tr class="datarow">
	  
	      <%
              String dateStr = new Date(record.getTimestamp()).toGMTString();
          %>
          <td class="envButton cell-label">
              <%=record.getTimestamp() %> (<%=dateStr %>) 
          </td>
          
          <td class="envButton cell-info-short">
              <%=record.getSequence() + 1 %> 
          </td>
          
	      <td class="cell-info2">
	          <a href="<%= record.getResultRecord().getUrl() %>"><%= record.getResultRecord().getUrl() %></a>
	      </td>	  	  	  	  
	  	       
          <td class="cell-info-short">
              <a href="<%= record.getResultRecord().getImageS3Url() %>" target="_blank">View</a>
          </td>
          
          <% if (record.getResultRecord().getAnalysisResult() != null) { %>
          <td class="cell-info2">
              <%=record.getResultRecord().getAnalysisResult().getColorResult() %> 
          </td>
          
          <td class="cell-info-short">
              <%=record.getResultRecord().getAnalysisResult().getXyResult() %> 
          </td>
          
          <td class="cell-info2">
              <%=record.getResultRecord().getAnalysisResult().getQuadResult() %>
          </td>      
          <% } %>
                  
	  </tr>
  <%
    }
  %>
  </table>
  
  </div>
</body>
</html>