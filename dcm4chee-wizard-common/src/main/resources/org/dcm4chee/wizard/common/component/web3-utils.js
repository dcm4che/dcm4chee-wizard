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
  Wicket.Ajax.registerFailureHandler(reloadPage);
}

function showMask() {
  try {
  	Wicket.MainPage.busyMask = new Wicket.MainPage.Mask(false);
  	Wicket.MainPage.current = this;
  	Wicket.MainPage.busyMask.show();
  } catch (ignore) {
  }
}

function hideMask() {
  try {
	Wicket.MainPage.busyMask.hide();
	Wicket.MainPage.busyMask = null;
  } catch (ignore) {
  }
}

function reloadPage() {
  window.location.reload(true);
}

/**
 * Taken from modal.js.
 * Transparent or semi-transparent masks that prevents user from interacting
 * with the portion of page behind a window. 
 *  
 */
Wicket.MainPage.Mask = Wicket.Class.create();

Wicket.MainPage.Mask.zIndex = 21000;

Wicket.MainPage.Mask.prototype = {
			
	initialize: function(transparent) {
		this.transparent = transparent;		
	},
	
	/**
	 * Shows the mask.
	 */
	show: function() {				
		
		// if the mask is not already shown...
		if (typeof(Wicket.MainPage.Mask.element) == "undefined" ||
			Wicket.MainPage.Mask.element == null) {		
		
			// crate the mask element and add  it to document
			var e = document.createElement("div");
			document.body.appendChild(e);							
			
			// set the proper css class name 
			if (this.transparent) {
				e.className = "wicket-mask-transparent";
			} else {
				e.className = "wicket-mask-dark";
			}					
		
			e.style.zIndex = Wicket.MainPage.Mask.zIndex;

			// HACK - KHTML doesn't support colors with alpha transparency
			// if the mask is not transparent we have to either
			// make the background image visible (setting color to transparent) - for KHTML
			// or make the background-image invisible (setting it to null) - for other browsers												
			if (this.transparent == false) {
				if (Wicket.Browser.isKHTML() == false) {			
					e.style.backgroundImage = "none";
				} else {
					e.style.backgroundColor = "transparent";
				}
			}

			// HACK - it really sucks that we have to set this to absolute even for gecko.
			// however background with position:fixed makes the text cursor in textfieds
			// in modal window disappear
			if (Wicket.Browser.isIE() || Wicket.Browser.isGecko()) {
				e.style.position = "absolute";
			}

			// set the element							 							
			this.element = e;
					
			// preserver old handlers
			this.old_onscroll = window.onscroll;
			this.old_onresize = window.onresize;
			
			// set new handlers
			window.onscroll = this.onScrollResize.bind(this);
			window.onresize = this.onScrollResize.bind(this);
			
			// fix the mask position
			this.onScrollResize(true);
						
			// set a static reference to mask
			Wicket.MainPage.Mask.element = e;			
		} else {
			// mask is already shown - don't hide it
			this.dontHide = true; 			
		}
		
		var doc = document;
		var old = Wicket.MainPage.current.oldWindow;
		if (typeof(old) != "undefined" && old != null) {
			doc = old.getContentDocument();
		}
		
		this.document = doc;
		
		// disable user interaction
		setTimeout(function() {this.hideSelectBoxes()}.bind(this), 300);
		setTimeout(function() {this.disableTabs()}.bind(this), 400);
		setTimeout(function() {this.disableFocus()}.bind(this), 1000); 			
	},
	
	/**
	 * Hides the mask.
	 */
	hide: function() {			
	
		// if the mask is visible and we can hide it
		if (typeof(Wicket.MainPage.Mask.element) != "undefined" && typeof(this.dontHide) == "undefined") {
	
			// remove element from document	
			document.body.removeChild(this.element);
			this.element = null;						
			
			// restore old handlers
			window.onscroll = this.old_onscroll;
			window.onresize = this.old_onresize;
						
			Wicket.MainPage.Mask.element = null;			
		}	
		
		// show old select boxes (ie only)
		this.showSelectBoxes();
		
		// restore tab order
		this.restoreTabs();
		
		// revert onfocus handlers
		this.enableFocus();
		
		this.document = null;

	},
	
	/**
	 * Used to update the position (ie) and size (ie, opera) of the mask.
	 */
	onScrollResize: function(dontChangePosition) {							
		// if the iframe is not position:fixed fix it's position
		if (this.element.style.position == "absolute") {
		
			var w = Wicket.MainPage.getViewportWidth();
			var h = Wicket.MainPage.getViewportHeight();
	
			var scTop = 0;
			var scLeft = 0;	 	
	 
 			scLeft = Wicket.MainPage.getScrollX();
			scTop = Wicket.MainPage.getScrollY();
	 		
			this.element.style.top = scTop + "px";
			this.element.style.left = scLeft + "px";
	
			if (document.all) { // opera or explorer
				this.element.style.width = w;
			}	
			this.element.style.height = h;		
		} 		
	},

	/**
	 * Returns true if 'element' is a child (anywhere in hierarchy) of 'parent'
	 */ 
	isParent: function(element, parent) {		
		if (element.parentNode == parent)
			return true;
		if (typeof(element.parentNode) == "undefined" ||
			element.parentNode == document.body)
			return false;
		return this.isParent(element.parentNode, parent);			
	},


	/**
	 * For internet explorer hides the select boxes (because they
	 * have always bigger z-order than any other elements).
	 */
	hideSelectBoxes : function() {				
		if (Wicket.Browser.isIE() && Wicket.Browser.isIE7() == false && 
				this.document != "undefined" && this.document != null) {
			var win = Wicket.MainPage.current;					
			
			this.boxes = new Array();
			var selects = this.document.getElementsByTagName("select");
			for (var i = 0; i < selects.length; i++) {				
				var element = selects[i];
				
				// if this is not an iframe window and the select is child of window content,
				// don't hide it					
				if (this.isParent(element, win.content)) {
					continue;
				}				
				
				if (element.style.visibility != "hidden") {
					element.style.visibility = "hidden";
					this.boxes.push(element);
				}				
			}
		}
	},
	
	/**
	 * Shows the select boxes if they were hidden. 
	 */
	showSelectBoxes: function() {
		if (typeof (this.boxes) != "undefined") {
			for (var i = 0; i < this.boxes.length; ++i) {
				var element = this.boxes[i];
				element.style.visibility="visible";
			}
			this.boxes = null;
		}		
	},
	
	/**
	 * Disable focus on element and all it's children.	 
	 */
	disableFocusElement: function(element, revertList) {
				
		if (typeof(Wicket.MainPage.current) != "undefined" &&
			Wicket.MainPage.current != null &&
			Wicket.MainPage.current.window != element) {								
				
			revertList.push([element, element.onfocus]);
			element.onfocus = function() { element.blur(); }			
			
			for (var i = 0; i < element.childNodes.length; ++i) {
				this.disableFocusElement(element.childNodes[i], revertList);
			}
		}
	},
	
	/**
	 * Disable focus on all elements in document
	 */
	disableFocus: function() {
		// explorer doesn't need this, because for IE disableTabs() is called.
		// plus in IE this causes problems because it scrolls document		);
		if (Wicket.Browser.isIE() == false && 
				this.document != "undefined" && this.document != null) {			
			this.focusRevertList = new Array();			
			var body = this.document.getElementsByTagName("body")[0];			
			for (var i = 0; i < body.childNodes.length; ++i) {		
				this.disableFocusElement(body.childNodes[i], this.focusRevertList);
			}
		}
	},
	
	/**
	 * Enables focus on all elements where the focus has been disabled.
	 */
	enableFocus: function() {
		if (typeof(this.focusRevertList) != "undefined") {						
			for (var i = 0; i < this.focusRevertList.length; ++i) {
				var item = this.focusRevertList[i];
				item[0].onfocus = item[1];
			}
		}
		this.focusRevertList = null;
	},	
	
	
	/**
	 * Disable tab indexes (ie).
	 */
	disableTabs: function () {		
		this.tabbableTags = new Array("A","BUTTON","TEXTAREA","INPUT","IFRAME", "SELECT");
		if (Wicket.Browser.isIE() && 
				this.document != "undefined" && this.document != null) {
			var win = Wicket.MainPage.current;			
			this.tabsAreDisabled = 'true';
			for (var j = 0; j < this.tabbableTags.length; j++) {
				var tagElements = this.document.getElementsByTagName(this.tabbableTags[j]);
				for (var k = 0 ; k < tagElements.length; k++) {
					
					// if this is not an iframe window and the element is child of window content,
					// don't disable tab on it
						if (this.isParent(tagElements[k], win.content) == false) {
						var element = tagElements[k];
						element.hiddenTabIndex = element.tabIndex;
						element.tabIndex="-1";
					}
				}
			}
		}
	},
	
	/**
	 * Restore tab indexes if they were disabled.
	 */
	restoreTabs: function() {
		if (typeof(this.tabsAreDisabled) != 'undefined') {
			for (var j = 0; j < this.tabbableTags.length; j++) {
				var tagElements = this.document.getElementsByTagName(this.tabbableTags[j]);
				for (var k = 0 ; k < tagElements.length; k++) {
					var element = tagElements[k];
					if (typeof(element.hiddenTabIndex) != 'undefined') {
						element.tabIndex = element.hiddenTabIndex;
						element.hiddenTabIndex = null;
					}
					element.tabEnabled = true;
				}
			}
			this.tabsAreDisabled = null;
		}
	}

}

Wicket.MainPage.getViewportHeight = function() {
	if (window.innerHeight != window.undefined) 
		return window.innerHeight;
	
	if (document.compatMode == 'CSS1Compat') 
		return document.documentElement.clientHeight;
		
	if (document.body) 
		return document.body.clientHeight;
		 
	return window.undefined; 
}

/**
 * Returns the width of visible area.
 */
Wicket.MainPage.getViewportWidth =  function() {
	if (window.innerWidth != window.undefined) 
		return window.innerWidth;
		 
	if (document.compatMode == 'CSS1Compat') 
		return document.documentElement.clientWidth; 
		
	if (document.body) 
		return document.body.clientWidth;
		 
	return window.undefined;
}

/**
 * Returns the horizontal scroll offset
 */
Wicket.MainPage.getScrollX = function() {
	var iebody = (document.compatMode && document.compatMode != "BackCompat") ? document.documentElement : document.body	
	return document.all? iebody.scrollLeft : pageXOffset
}

/**
 * Returns the vertical scroll offset
 */
Wicket.MainPage.getScrollY = function() {
	var iebody = (document.compatMode && document.compatMode != "BackCompat") ? document.documentElement : document.body	
	return document.all? iebody.scrollTop : pageYOffset
}
