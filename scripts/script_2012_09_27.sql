/***************************************************************
* 27-sep-2012
* By Ricardo Memoria
* 
* FIX DATABASE INCONSISTENCIES GENERATED BY MYSQL
* 
****************************************************************/

delete from examculture where case_id is null;
ALTER TABLE `examculture` MODIFY COLUMN `CASE_ID` INT(11) NOT NULL;

delete from exammicroscopy where case_id is null;
ALTER TABLE examculture MODIFY COLUMN `CASE_ID` INT(11) NOT NULL;

delete from examdst where case_id is null;
ALTER TABLE examdst MODIFY COLUMN `CASE_ID` INT(11) NOT NULL;

delete from examdst where laboratory_id is not null and laboratory_id not in (select id from laboratory);
delete from examculture where laboratory_id is not null and laboratory_id not in (select id from laboratory);
delete from exammicroscopy where laboratory_id is not null and laboratory_id not in (select id from laboratory);