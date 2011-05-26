/***************************************************************
* Inclusion of 2 new permissions to control case comments
* Include tables for case comments and modifications in comorbidity
* 
* By Ricardo Memoria - 21-mar-2011
****************************************************************/

insert into UserRole (id, changeable, code, Role_Name, internalUse, byCaseClassification)
values (165, false, '010115', 'CASE_TAG', false, true);


insert into UserRole (id, changeable, code, Role_Name, internalUse, byCaseClassification)
values (166, false, '010115', 'ADM_TAGS', false, true);

