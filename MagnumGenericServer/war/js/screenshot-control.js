$(function() {
    	$('#resTable').hide();	
    });
    
    function captureScreenshot() {
    	$('#resTable').hide();
   
    	var siteUrl = $('#siteurl').val();
    	var screenshotUrl = "/v1/bbds/screenshot";    
    	
    	$('#gobutton').attr('disabled', 'disabled');
    	$("#processingImg").attr("src", "./icons/processing.gif");
    	$("#xyProcessingImg").attr("src", "./icons/processing.gif");
    	$("#colorProcessingImg").attr("src", "./icons/processing.gif");
    	$("#quadtreeProcessingImg").attr("src", "./icons/processing.gif");
    	$("#processingImg").attr("width", "24");
    	$("#processingImg").attr("height", "24");
        
    	$.ajax(
            {
                type : "GET",
                url  : screenshotUrl,
                data : {
                	url : siteUrl
                },
                success : function(result) {
                	$('#resTable').show();
                	$('#colorResult').text('');
                	$('#xyResult').text('');
                	$('#quadtreeResult').text('');
                	$('#gobutton').removeAttr("disabled");
                    $('#processingImg').removeAttr("src");
                    $('#processingImg').removeAttr("width");
                    $('#processingImg').removeAttr("height");
                    
                    var resultData = jQuery.parseJSON( result );                    
                    // update href
                    $("#resultImageUrl").attr("href", resultData.imageS3Url);
                    $("#resultImageUrl").text(resultData.imageS3Url);
                    // update img
                    $("#resultImageImg").attr("src", resultData.imageS3Url);  
                    $("#resultImageImg").attr("width", 600);  
                    
                    if (resultData.success) {
                    	$.ajax(
                                {
                                    type : "GET",
                                    url  : "/v1/bbds/analyze/color",
                                    data : {
                                        id : resultData.id
                                    },
                                    success : function(result) {                   
                                    	$('#colorProcessingImg').removeAttr("src");
                                    	$('#colorResult').text(result);
                                    },
                                    error: function (jqXHR, exception) {
                                    	$('#colorProcessingImg').removeAttr("src");
                                    }
                                });
                    	$.ajax(
                                {
                                    type : "GET",
                                    url  : "/v1/bbds/analyze/quadtree",
                                    data : {
                                        id : resultData.id
                                    },
                                    success : function(result) {
                                    	$('#quadtreeProcessingImg').removeAttr("src");
                                        $('#quadtreeResult').text(result);
                                    },
                                    error: function (jqXHR, exception) {
                                    	$('#quadtreeProcessingImg').removeAttr("src");
                                    }
                                });
                    	$.ajax(
                                {
                                    type : "GET",
                                    url  : "/v1/bbds/analyze/xy",
                                    data : {
                                        id : resultData.id
                                    },
                                    success : function(result) {
                                    	$('#xyProcessingImg').removeAttr("src");
                                        $('#xyResult').text(result);
                                    },
                                    error: function (jqXHR, exception) {
                                    	$('#xyProcessingImg').removeAttr("src");
                                    }
                                });
                    }
                },
                error: function (jqXHR, exception) {
                    alert("Failed to capture the screenshot.");
                    
                    $('#gobutton').removeAttr("disabled");
                    $('#processingImg').removeAttr("src");
                    $('#processingImg').removeAttr("width");
                    $('#processingImg').removeAttr("height");
                }
            });
    }