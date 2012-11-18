/***************************************************************
* Inclusion of 2 new permissions to control case comments
* Include tables for case comments and modifications in comorbidity
* 
* By Ricardo Memoria - 21-mar-2011
****************************************************************/

insert into UserRole (id, changeable, code, Role_Name, internalUse, byCaseClassification)
values (165, false, '010115', 'CASE_TAG', false, true);


insert into UserRole (id, changeable, code, Role_Name, internalUse, byCaseClassification)
values (162, false, '010115', 'TAGS', false, true);


insert into UserRole (id, changeable, code, Role_Name, internalUse, byCaseClassification)
values (166, false, '010200', 'CASE_CHANGENUMBER', false, false);


INSERT INTO UserPermission
(canchange, canexecute, grantPermission, profile_id, role_id)
select false, false, true, id, 138 from userprofile
where name like "%adminis%"
and not exists (select * from UserPermission where role_id=138 and profile_id=userprofile.id);


alter table tags_case
add  CONSTRAINT `FK4577A796A3F04EEB` FOREIGN KEY (`CASE_ID`) REFERENCES `tbcase` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
