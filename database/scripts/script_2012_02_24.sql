/***************************************************************
* By Ricardo Memoria - 2012 feb 24th
****************************************************************/

alter table systemconfig
 add pageRootURL varchar(200);

update systemconfig
 set pageRootURL = concat(systemURL, '/etbmanager');

alter table workspace
 add sendSystemMessages boolean;

update workspace
 set sendSystemMessages = false;