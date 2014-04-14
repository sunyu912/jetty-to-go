function startOptimizer() {
	var candidates = new Array();
	$('.appCheckbox').each(function(index, value) {		
		if ($(this).attr('checked')) {
			var containerId = $(this).attr('name');
			var throughput = $('#throughput-' + index).val();
			var latency = $('#latency-' + index).val();			
			var paramStr = containerId + "@" + throughput + "@" + latency;
			candidates.push(paramStr);
		}
	});
	
	var enableCotest = false;
	$('.optionCheckbox').each(function(index, value) {		
		if ($(this).attr('checked')) {
			enableCotest = true;
		}
	});
	
	console.log(candidates);
	if (candidates) {
		triggerSolution(candidates, enableCotest);
	}
}

function triggerSolution(candidates, cotest) {	
	$.ajax(
	{
		type : "GET",
		url  : "/v1/roar/packing/solution",
		traditional : true,
		timeout : 60 * 1000 * 10,
		data : {
			candidate : candidates,
			enableCotest : cotest
		},
		success : function(result) {                   
			$('#resultSection').html(result);
		},
		error: function (jqXHR, exception) {
			console.log("Faiiled to load result solution.");
		}
	});
}