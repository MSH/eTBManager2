/***************************************************************
* By Ricardo Memoria - 2012 feb 1st
****************************************************************/
insert into userrole (id, changeable, code, role_name, messagekey, internaluse, bycaseclassification) 
	values (184, false, '010402', 'CASE_STARTTREAT', 'cases.details.starttreatment', true, false);


update userrole set messagekey = 'userrole.NEWSUSP' where id = 170;
update userrole set messagekey = 'userrole.NEWCASE' where id = 171;
update userrole set messagekey = 'userrole.CASE_VIEW' where id = 100;
update userrole set messagekey = 'userrole.CASE_INTAKEMED' where id = 141;
update userrole set messagekey = 'userrole.CASE_DEL_VAL' where id = 143;
update userrole set messagekey = 'userrole.CASE_PERSONAL_VIEW' where id = 147;
update userrole set messagekey = 'userrole.CASE_COMMENTS' where id = 159;
update userrole set messagekey = 'userrole.REM_COMMENTS' where id = 160;
update userrole set messagekey = 'userrole.CASE_TAG' where id = 165;
update userrole set messagekey = 'userrole.CASE_CHANGENUMBER' where id = 166;
update userrole set messagekey = 'userrole.REM_COM' where id = 161;
update userrole set messagekey = 'userrole.EXAM_BIOMOL' where id = 172;
update userrole set messagekey = 'userrole.SEND_ORDER' where id = 14;
update userrole set messagekey = 'userrole.RECEIV_ORDER' where id = 15;
update userrole set messagekey = 'userrole.DISP_PAC' where id = 16;
update userrole set messagekey = 'userrole.TRANSFER' where id = 46;
update userrole set messagekey = 'userrole.NEW_TRANSFER' where id = 145;
