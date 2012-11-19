/****************************************************************
 * Include a new user role to register in transaction log the
 * changes in medical consultation
 * 
 * By Ricardo Memoria
 * 19-nov-2012
 * 
 ****************************************************************/

insert into userrole (id, changeable, code, role_name, internaluse, bycaseclassification, messagekey) values
(195, true, '011014', 'CASE_MED_EXAM', true, true, '');