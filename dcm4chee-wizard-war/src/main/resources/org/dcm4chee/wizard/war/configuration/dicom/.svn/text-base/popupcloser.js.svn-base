window.onload = setupMainPage;

if (typeof(Wicket) == "undefined") {
	Wicket = { };
}


if (Wicket.Class == null) {
	Wicket.Class = {
		create: function() {
			return function() {
				this.initialize.apply(this, arguments);
			}
		}
	};
}

Wicket.MainPage = Wicket.Class.create();

function setupMainPage() {
  Wicket.Ajax.registerFailureHandler(closePage);
}


function closePage() {
	alert("Session timeout! Close this window.");
	self.close();
}
