$(function() {
	$("#targetType").select2();
	
	$('#backButton').on('click', function() {
		var url = window.location.href;
		url = url.substr(0, url.indexOf('/workflow/') + 9);
		window.location.href = url;
	});
	
	function submitForm() {
		$('#workflowForm').validate();
		$('#workflowForm').submit();
	}
	
	$('#workflowForm input, #workflowForm textarea, #workflowForm select').on('change', submitForm);
});