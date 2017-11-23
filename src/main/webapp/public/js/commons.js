
A4J.AJAX.onError=function(req, status, message) {
	//window.alert(window.errortitle + '... Request=' + req + ', Status=' + status + ', Message=' +  message);
	window.alert(window.errortitle);
	closeWaitDlg();
}
// change workspace to the given workspace id
function changeWorkspace(id) {
	var s=window.appcontextpath + '/changeworkspace.seam?id=' + id;
	location.href=s;
}
function unhoverWorkspace(){jQuery('.workspace-data').removeClass('workspace-data-selected');}
// change the current language
function changeLang(lang) {
	var s=document.location.href;
	var slang='lang='+lang;
	if (s.indexOf('lang=')>=0) {
		var currlang='lang=' + window.etbmlang;
		document.location.href=s.replace(currlang,slang);
	}
	else {
		s+=(s.indexOf('?')==-1?'?':'&')+slang; 
		document.location.href=s;
	}
}
function showModalAutoTop(name, val) {
	val = (windowPosTop()+(val?val:100))+'px';
	Richfaces.showModalPanel(name, {top:val});
}
function windowPosTop() { return jQuery("body").scrollTop(); }
// disable a specific button
function disableButton(button) {
	if (jQuery(button).is(".button-disabled"))	return false;
	var el=jQuery('span', button);
	var s=el.text();
	el.html('<div class="wait-icon2"/>' + s);
	jQuery(button).addClass("button-disabled");
	return true;
}
// enable all buttons of the page
function enableButton() {
	jQuery('.button-disabled').each(function() {
	    var s=jQuery(this).removeClass("button-disabled").text();
	    jQuery('span',this).html(s);
	});
}
// startup call when document is loaded
jQuery(document).ready(function() {
  try {
    if (jQuery.browser.msie && jQuery.browser.version >= 9) {
      window.XMLSerializer = function() {};
      window.XMLSerializer.prototype.serializeToString = function(oNode) {
        return oNode.xml;
      };
    }
  } catch (exception) {}
  try {
      A4J.AJAX.XMLHttpRequest.prototype._copyAttribute = function(src, dst, attr) {
          var value = src.getAttribute(attr);
          if (value) {
            try {
            dst.setAttribute(attr, value);
            } catch (err) {}
          }
      };
   } catch (exception) {}
	 
    jQuery('.workspace-data').hover(function(){
    	jQuery(this).addClass('workspace-data-selected');
    }, function(){
    	jQuery(this).removeClass('workspace-data-selected');
    });
});

// open the wait window when requesting data from server 
function openWaitDlg() {
	jQuery('#waitdiv').show();
}
// close the wait window when request is complete
function closeWaitDlg() {
	jQuery('#waitdiv').hide();
}


function numbersOnly(myfield, e, dec) {
var key;
var keychar;

if (window.event)
   key = window.event.keyCode;
else if (e)
   key = e.which;
else
   return true;
keychar = String.fromCharCode(key);

// control keys
if ((key==null) || (key==0) || (key==8) || 
    (key==9) || (key==13) || (key==27) )
   return true;

// numbers
else if ((("0123456789").indexOf(keychar) > -1))
   return true;

// decimal point jump
else if (dec && (keychar == dec))
   {
   myfield.form.elements[dec].focus();
   return false;
   }
else
   return false;
}

function numbersOnly(myfield, e, dec, maxlenght,blockOnlyDecimal) {
	var key;
	var keychar;

	//check max lenght
	if(myfield.value.length >=maxlenght)
		return false;

	if (window.event)
		key = window.event.keyCode;
	else if (e)
		key = e.which;
	else
		return true;
	keychar = String.fromCharCode(key);

	//check only decimal
	if (blockOnlyDecimal && dec && (keychar == dec) && (myfield.value == '0' || myfield.value == ''))
		return false;

// control keys
	if ((key==null) || (key==0) || (key==8) ||
		(key==9) || (key==13) || (key==27) )
		return true;

// numbers
	else if ((("0123456789").indexOf(keychar) > -1))
		return true;

// decimal point jump
	else if (dec && (keychar == dec))
	{
		myfield.form.elements[dec].focus();
		return false;
	}
	else
		return false;
}

function checkValid(elem1, elem2, elem3) {
  val1 = parseInt(elem1.value);
  if (isNaN(val1))
  	return;
}


function changeQtd(elem, evt) {
  if (!getElements(elem, evt))
  	return;
  if ((isNaN(qtdA)) || (qtdA == 0))
  	return;
  	
  if (edtC.value == '')
	  prev = edtB;
  if ((!isNaN(qtdB)) && (qtdB != 0) && (prev != edtC))
     edtC.value = qtdA/qtdB;
  else
  if ((!isNaN(qtdB)) && (qtdB != 0))
     edtB.value = qtdA/qtdC; 
}


function changeCon(elem, evt) {
  if (!getElements(elem, evt)) 
  	return;
  if ((isNaN(qtdB)) || (qtdB == 0))
  	return;
  
  if (edtC.value == '')
	  prev = edtA;
  if ((!isNaN(qtdA)) && (qtdA != 0) && (prev != edtC))
     edtC.value = qtdA/qtdB;
  else
  if ((!isNaN(qtdC)) && (qtdC != 0))
     edtA.value = qtdB*qtdC; 
}

function changeQtdCon(elem, evt) {
  if (!getElements(elem, evt))
  	return;
  if ((isNaN(qtdC)) || (qtdC == 0))
  	return;
  
  if (edtB.value == '')
	  prev = edtA;
  if ((!isNaN(qtdA)) && (qtdA != 0) && (prev != edtB))
     edtB.value = qtdA/qtdC;
  else
  if ((!isNaN(qtdB)) && (qtdB != 0))
     edtA.value = qtdB*qtdC; 
}

function getElements(elem, e) {
  if (window.event)
  	c = window.event.keyCode;
  else c = e.which;
  if ((c != 8)&&(c<32))
  	return false;

  if (window.atual!=elem) {
    window.prev = window.atual;
  	window.atual = elem;
  }

  window.edtA = document.getElementById("formbatch:qtd:qtd");
  window.edtB = document.getElementById("formbatch:num:num");
  window.edtC = document.getElementById("formbatch:qtdcon:qtdcon");

  window.qtdA = parseInt(edtA.value);
  window.qtdB = parseInt(edtB.value);
  window.qtdC = parseInt(edtC.value);
  return true;
}

function strToFloat(sval) {
   if (window.decSeparator)
	   sval = sval.replace(window.decSeparator, ".");
   return parseFloat(sval);
}

function floatToStr(val, n) {
	var s = val.toFixed(n);
	if (window.decSeparator)
		s = s.replace(".", window.decSeparator);
	return s;
}

function changeUnitPrice(elem) {
	var unit = strToFloat(elem.value);
	var tot = parseFloat(document.getElementById("formbatch:qtd:qtd").value);
	var totPrice = unit * tot;
	document.getElementById("formbatch:tprice:tprice").value = floatToStr(totPrice, 2);
}

function changeUPrice() {
	var unit = strToFloat(document.getElementById("formbatch:uprice:uprice").value);
	var tot = parseFloat(document.getElementById("formbatch:qtd:qtd").value);
	var totPrice = unit * tot;
	document.getElementById("formbatch:tprice:tprice").value = floatToStr(totPrice, 2);
}

function changeTotalPrice(elem) {
	var totPrice = strToFloat(elem.value);
	var tot = parseFloat(document.getElementById("formbatch:qtd:qtd").value);
	var unitPrice;
	if (tot != 0)
		unitPrice = totPrice / tot;
	else unitPrice = 0;
	document.getElementById("formbatch:uprice:uprice").value = floatToStr(unitPrice, 2);
}

function changeBMI(elem, e) {
	var c;
	if (window.event)
  		c = window.event.keyCode;
  	else c = e.which;
  	if ((c != 8)&&(c<32))
  		return -1;
  	
  	s = jQuery('#edtweight input').val();
  	var weight = strToFloat(s);
  	s = jQuery('#edtheight input').val();
	var height = strToFloat(s);
	if ((weight == 0) || (height == 0) || (isNaN(weight)) || (isNaN(height)))
		return 0;
	height = height / 100;
	return  weight/(height * height);
}
function updateBMI(elem, event) {
	var val = changeBMI(elem,event);
	if (val == -1)
		return;
	
	if (val == 0)
		val = '-';
	else val = floatToStr(val, 4);
	
	jQuery('#divBMI').html(val);
}

