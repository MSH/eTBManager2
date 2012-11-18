/****************************************************************
 * 22-10-2012
 * By A.M.
 * 
 * ONLY FOR AZERBAIJAN
 * Add new field to TbCaseAZ - date of entering to EIDSS
 ****************************************************************/

ALTER TABLE `etbmanager`.`tbcaseaz` ADD COLUMN `inEIDSSDate` DATE NULL DEFAULT NULL  AFTER `systemDate` ;

