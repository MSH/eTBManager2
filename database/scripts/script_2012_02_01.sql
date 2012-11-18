/***************************************************************
* By Ricardo Memoria - 2012 feb 1st
****************************************************************/

alter table transactionlog
 add entityClass varchar(100);

update transactionlog
set entityClass = 'TbCase' where role_id in (select id from userrole where code like '01%');

insert into userrole (id, changeable, code, role_name, internaluse, bycaseclassification) values (180, false, '040800', 'NEWPWD', true, false);

update transactionlog
set entityClass = 'User' where role_id in (2, 180);

alter table userrole
  add messageKey varchar(100);

insert into userrole (id, changeable, code, role_name, messagekey, internaluse, bycaseclassification) 
	values (181, false, '010409', 'CASE_TRANSFERIN', 'cases.move.regtransferin', true, false);

insert into userrole (id, changeable, code, role_name, messagekey, internaluse, bycaseclassification) 
	values (182, false, '010409', 'CASE_TRANSFEROUT', 'cases.move', true, false);

insert into userrole (id, changeable, code, role_name, messagekey, internaluse, bycaseclassification) 
	values (183, false, '010409', 'CASE_TRANSFERCANCEL', 'cases.move.cancel', true, false);

update userrole set messagekey = 'admin.users.newpasswd' where id = 180;
update userrole set messagekey = 'admin' where id = 1;

update tbcase set treatment_unit_id = (select unit_id from treatmenthealthunit a where a.case_id = tbcase.id
and inidate = (select max(inidate) from treatmenthealthunit b where b.case_id=tbcase.id) limit 1);

update userrole set messagekey = 'admin.adminunits' where id = 3;
update userrole set messagekey = 'admin.sources' where id = 5;
update userrole set messagekey = 'admin.tbunits' where id = 6;
update userrole set messagekey = 'admin.medicines' where id = 7;
update userrole set messagekey = 'admin.users' where id = 2;
update userrole set messagekey = 'admin.regimens' where id = 40;
update userrole set messagekey = 'admin.workspaces' where id = 41;
update userrole set messagekey = 'admin.workspaces.copy' where id = 135;
update userrole set messagekey = 'admin.profiles' where id = 39;
update userrole set messagekey = 'admin.labs' where id = 50;
update userrole set messagekey = 'admin.substances' where id = 52;
update userrole set messagekey = 'admin.weeklyfreq' where id = 53;
update userrole set messagekey = 'admin.fields' where id = 58; 
update userrole set messagekey = 'admin.healthsys' where id = 65;
update userrole set messagekey = 'admin.ageranges' where id = 117;
update userrole set messagekey = 'admin.tags' where id = 162;
update userrole set messagekey = 'admin.import' where id = 136;
update userrole set messagekey = 'admin.syssetup' where id = 64;
update userrole set messagekey = 'cases.close' where id = 103;
update userrole set messagekey = 'cases.details.case' where id = 140;
update userrole set messagekey = 'cases.details.treatment' where id = 106;
update userrole set messagekey = 'cases.details.exams' where id = 105;
update userrole set messagekey = 'cases.details.otherinfo' where id = 146;
update userrole set messagekey = 'cases.details.report1' where id = 142;
update userrole set messagekey = 'cases.validate' where id = 101;
update userrole set messagekey = 'cases.move' where id = 102;
update userrole set messagekey = 'cases.reopen' where id = 104;

update userrole set messagekey = 'cases.examculture' where id = 130;
update userrole set messagekey = 'cases.exammicroscopy' where id = 131;
update userrole set messagekey = 'cases.examdst' where id = 132;
update userrole set messagekey = 'cases.examhiv' where id = 133;
update userrole set messagekey = 'cases.examxray' where id = 134;
update userrole set messagekey = 'cases.comorbidities' where id = 152;
update userrole set messagekey = 'cases.contacts' where id = 153;
update userrole set messagekey = 'cases.sideeffects' where id = 154;
update userrole set messagekey = 'cases.exambiopsy' where id = 157;
update userrole set messagekey = 'cases.examskintest' where id = 158;
update userrole set messagekey = 'admin.websessions' where id = 19;
update userrole set messagekey = 'admin.reports.usersession' where id = 63;

update userrole set messagekey = 'cases' where id = 49;
update userrole set messagekey = 'medicines' where id = 9;
update userrole set messagekey = 'meds.receiving' where id = 11;
update userrole set messagekey = 'meds.orders' where id = 18;
update userrole set messagekey = 'meds.orders.new' where id = 144;
update userrole set messagekey = 'meds.orders.cancel' where id = 25;
update userrole set messagekey = 'meds.orders.autorize' where id = 13;
