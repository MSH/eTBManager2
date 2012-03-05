/***************************************************************
* By Ricardo Memoria - 2012 mar 1st
****************************************************************/

alter table sys_user add sendSystemMessages boolean;

update sys_user set sendSystemMessages=true;