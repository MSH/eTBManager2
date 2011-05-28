/***************************************************************************************
* New permission to change case number
* may 27, 2011
****************************************************************************************/


insert into UserRole (id, changeable, code, Role_Name, internalUse, byCaseClassification)
values (166, false, '010200', 'CASE_CHANGENUMBER', false, true);
