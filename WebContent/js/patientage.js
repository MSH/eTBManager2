function handleCaseDateChange() {
	sBirth = jQuery("#divbirthdate input[type='text']").val();
	sReg = jQuery("#divregdate input[type='text']").val();
	if (!(sReg))
		 dt2 = new Date();
	else dt2 = convertDate(sReg)
	var dt1=convertDate(sBirth);
	if (dt1==null)
		return;
	window.formato = jQuery("#formatodata").val();
	
	var age = "";
	if (dt1!=null) {
		age = yearsBetween(dt1, dt2);
	}
	jQuery("#divage input[type='text']").val(age);
}

//Change the algorithm calculating age for Ukraine and Azerbaijan
function handleCaseDateChangeUAZ() {
	sBirth = jQuery("#divbirthdate input[type='text']").val();
	sReg = jQuery("#divregdate input[type='text']").val();
	if (!(sReg))
		 dt2 = new Date();
	else dt2 = convertDate(sReg);
	var dt1=convertDate(sBirth);
	if (dt1==null)
		return;
	window.formato = jQuery("#formatodata").val();
	
	var age=0;
	if (dt1!=null) {
		if (dt1<=dt2){
			age = dt2.getFullYear() - dt1.getFullYear();
			dt2.setFullYear(dt2.getFullYear()-age);
			if (dt1>dt2) 
				age-=1;
		}
		else age = "Error date value";
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

function agetodob(id) {
	var age = id.value;
	var curDt = new Date();
	
	var mon   = curDt.getMonth() + 1;
	var year  = curDt.getFullYear();
	
	var dobyr = year - age;
	
	if(mon<6){
	var strdob = "01"+"/"+"01"+"/"+dobyr;	
		}
		
	if(mon>6){
	var strdob = "01"+"/"+"07"+"/"+dobyr;
	}
	
	jQuery("#divbirthdate input[type='text']").val(strdob);
	;
}