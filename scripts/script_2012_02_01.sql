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

