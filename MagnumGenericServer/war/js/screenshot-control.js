$(function() {
 $('#resTable').hide();	
 stopSpinning();
 resetTables('N/A');
});

$(function() {
 $('#resultImageImg').width($('#resultImageImg').parent().width()); 
 //$('#resultImageImg').parent().height(440); 
});

function startSpinning() {
 $("#screenshotSpinner").addClass( "fa-refresh fa-spin" );
 $("#colorSpinner").addClass( "fa-refresh fa-spin" );
 $("#quadSpinner").addClass( "fa-refresh fa-spin" );
 $("#xySpinner").addClass( "fa-refresh fa-spin" );
}

function stopSpinning() {
 $("#screenshotSpinner").removeClass( "fa-refresh fa-spin" );
 $("#colorSpinner").removeClass( "fa-refresh fa-spin" );
 $("#quadSpinner").removeClass( "fa-refresh fa-spin" );
 $("#xySpinner").removeClass( "fa-refresh fa-spin" ); 
}

function resetTables(status) {
    $('#color1').text(status);
    $('#color2').text(status);
    $('#leaves').text(status);
    $('#textgroup').text(status);
    $('#imagearea').text(status);
    $('#maxdec').text(status);
    $('#horbal').text(status);
    $('#horsym').text(status);
    $('#verbal').text(status);
    $('#versym').text(status);
}

function captureScreenshot() {
 $('#resTable').hide();

 var siteUrl = $('#siteurl').val();
 var screenshotUrl = "/v1/bbds/screenshot";    

 $('#gobutton').prop('disabled', true);

 startSpinning();
 resetTables('processing ...');

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

     var resultData = jQuery.parseJSON( result );                    
                    // update href
                    $("#resultImageUrl").attr("href", resultData.imageS3Url);
                    //$("#resultImageUrl").text(resultData.imageS3Url);
                    // update img
                    $("#resultImageImg").attr("src", resultData.imageS3Url);  

                    $('#resultImageImg').width($('#resultImageImg').parent().width());

                    $("#screenshotSpinner").removeClass( "fa-refresh fa-spin" );
                    
                    if (resultData.success) {
                    	$.ajax(
                        {
                            type : "GET",
                            url  : "/v1/bbds/analyze/color",
                            data : {
                                id : resultData.id
                            },
                            success : function(result) {                   
                             $("#colorSpinner").removeClass( "fa-refresh fa-spin" );

                                    	//$('#colorResult').text(result);
                                        var resultObj = JSON.parse(result);

                                        $('#color1').text(resultObj.colorfulness1);
                                        $('#color2').text(resultObj.colorfulness2);
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
                             $("#quadSpinner").removeClass( "fa-refresh fa-spin" );

                                        // $('#quadtreeResult').text(result);
                                        var resultObj = JSON.parse(result);

                                        $('#horbal').text(resultObj.horizontalBalance);
                                        $('#horsym').text(resultObj.horizontalSymmetry);
                                        $('#verbal').text(resultObj.verticleBalance);
                                        $('#versym').text(resultObj.verticleSymmetry);
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
     $("#xySpinner").removeClass( "fa-refresh fa-spin" );

     var resultObj = JSON.parse(result);

     $('#leaves').text(resultObj.numOfLeaves);
     $('#textgroup').text(resultObj.numOfTextGroup);
     $('#imagearea').text(resultObj.numOfImageArea);
     $('#maxdec').text(resultObj.maxDecompositionLevel);

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