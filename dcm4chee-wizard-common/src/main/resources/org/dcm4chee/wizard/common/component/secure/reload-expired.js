window.onload = setupReload;

function setupReload() {
	Wicket.Event.subscribe('/ajax/call/failure', function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
		parent.location.reload();
	});
}
