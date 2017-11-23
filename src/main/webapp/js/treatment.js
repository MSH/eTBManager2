jQuery(document).ready(function () {
	popupTreatment = null;
	popupLong = false;
	createBarEventLinks();
});

function createBarEventLinks() {
	jQuery(".bar,.bar-header").click(function() {
		var aux = jQuery(".popup", this);
		// is long popup ?
		if (aux.size() == 0) {
			aux = jQuery(".popup-long", this);
			if (aux.is(':hidden')) {
				if (popupTreatment != null)
					closePopupHint(popupTreatment);
				popupLong = true;
				aux.animate({opacity: "show", top: "-240px"}, "slow");
				popupTreatment = aux;
			}
			else closePopupHint(aux);
		}
		else {
			if (aux.is(':hidden')) {
				if (popupTreatment != null)
					closePopupHint(popupTreatment);
				popupLong = false;
				aux.animate({opacity: "show", top: "-120px"}, "slow");
				popupTreatment = aux;
			}
			else closePopupHint(aux);
		}
	});

	jQuery('.bar-header').mouseenter(function() {
		jQuery(this).removeClass('bar-header');
		jQuery(this).addClass('bar-header-active');
	});

	jQuery('.bar-header').mouseleave(function() {
		jQuery(this).removeClass('bar-header-active');
		jQuery(this).addClass('bar-header');
	});

	jQuery('.bar').mouseenter(function() {
		jQuery(this).removeClass('bar');
		jQuery('.bar-left', this).addClass('bar-left-active').removeClass('bar-left');
		jQuery('.bar-right', this).addClass('bar-right-active').removeClass('bar-right');

		jQuery(this).addClass('bar-active');
	});

	jQuery('.bar').mouseleave(function() {
		jQuery(this).removeClass('bar-active');
		jQuery('.bar-left-active', this).addClass('bar-left').removeClass('bar-left-active');
		jQuery('.bar-right-active', this).addClass('bar-right').removeClass('bar-right-active');

		jQuery(this).addClass('bar');		
	});

	jQuery('.popup #closewin').click( function() {
		closePopupHint(jQuery(this).parent('.popup'));
	});
	jQuery('.popup-long #closewin').click( function() {
		closePopupHint(jQuery(this).parent('.popup'));
	});
}

function closePopupHint(jqryElem, isLong) {
	if (popupLong)
		 jqryElem.animate({opacity: "hide", top: "-250px"}, "fast");
	else jqryElem.animate({opacity: "hide", top: "-130px"}, "fast");
	popupTreatment = null;	
}

function showMedicineDlg() {
	Richfaces.showModalPanel('medicinedlg');
}