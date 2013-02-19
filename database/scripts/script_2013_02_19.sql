/***************************************************************
 * 19-feb-2013
 * By A.M.
 * 
 * UA only!!! Add new field to CaseDataUA
 * 
****************************************************************/

ALTER TABLE `etbmanager`.`casedataua` ADD COLUMN `refuse2line` BIT(1) NOT NULL  AFTER `causeChangeTreat_id` ;



