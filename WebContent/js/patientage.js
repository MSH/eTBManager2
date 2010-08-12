function handleCaseDateChange() {
	sBirth = jQuery("#divbirthdate input[type='text']").val();
	sReg = jQuery("#divregdate input[type='text']").val();
	if (!(sReg))
		sReg = "01/05/2009";
	var dt1=convertDate(sBirth);
	var dt2=convertDate(sReg);
	window.formato = jQuery("#formatodata").val();
	
	var age = "";
	if (dt1!=null) {
		if (dt2==null)
			dt2 = new Date();
		age = yearsBetween(dt1, dt2);
	}
	
	jQuery("#divage input[type='text']").val(age);
}

function convertDate(s) {
	dia = s.replace(/_/g," ").substring(0,2);
	mes = s.replace(/_/g," ").substring(3,5);
	ano = s.replace(/_/g," ").substring(6,10);

	if (window.formato == "MDY") {
		a=dia; dia=mes; mes=a;
	}
	else
	if (window.formato == "YMD") {
		a=dia; dia=ano; ano=dia;
	}

	if ((ano < 1900) || (mes == 0) || (mes > 12))
		return null;

	var dt = new Date();

	try {
		dt.setFullYear(ano, mes-1, dia);
	} catch(e) {dt = null;}
	return dt;
}

function yearsBetween(dt1, dt2) {
	var milli=dt2-dt1;
	var milliPerYear=1000*60*60*24*365.26;

	return Math.floor(milli/milliPerYear);
}