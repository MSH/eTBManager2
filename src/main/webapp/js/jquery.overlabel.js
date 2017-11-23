oldJQuery.fn.labelOver = function(overClass) {
	return this.each(function(){
		var label = oldJQuery(this);
		var f = label.attr('for');
		if (f) {
			f = document.getElementById(f);
			var input = oldJQuery(f);
			
			this.hide = function() {
			  label.css({ textIndent: -10000 })
			}
			
			this.show = function() {
			  if (input.val() == '') label.css({ textIndent: 0 })
			}

			// handlers
			input.bind("focus", this, function(e) { e.data.hide();});
			input.bind("blur", this, function(e) { e.data.show();});
		  label.addClass(overClass).click(function(){ input.focus() });

			if (input.val() != '') {
				this.hide();
			}
		}
	})
}