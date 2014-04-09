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
	console.log(candidates);
	if (candidates) {
		triggerSolution(candidates);
	}
}

function triggerSolution(candidates) {
	$.ajax(
	{
		type : "GET",
		url  : "/v1/roar/packing/solution",
		traditional : true,
		data : {
			candidate : candidates
		},
		success : function(result) {                   
			$('#resultSection').html(result);
		},
		error: function (jqXHR, exception) {
			console.log("Faiiled to load result solution.");
		}
	});
}