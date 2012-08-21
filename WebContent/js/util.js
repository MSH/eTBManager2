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
	unit = strToFloat(elem.value);
	tot = parseFloat(document.getElementById("formbatch:qtd:qtd").value);
	totPrice = unit * tot;
	document.getElementById("formbatch:tprice:tprice").value = floatToStr(totPrice, 2);
}

function changeUPrice() {
	alert('1');
	unit = strToFloat(document.getElementById("formbatch:uprice:uprice").value);
	alert('2');
	tot = parseFloat(document.getElementById("formbatch:qtd:qtd").value);
	alert('3');
	totPrice = unit * tot;
	alert('4');
	document.getElementById("formbatch:tprice:tprice").value = floatToStr(totPrice, 2);
}

function changeTotalPrice(elem) {
	totPrice = strToFloat(elem.value);
	tot = parseFloat(document.getElementById("formbatch:qtd:qtd").value);
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
  	weight = strToFloat(s);
  	s = jQuery('#edtheight input').val();
	height = strToFloat(s);
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

