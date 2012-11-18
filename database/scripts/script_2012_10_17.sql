/****************************************************************
 * 15-10-2012
 * By A.M.
 * 
 * ONLY FOR AZERBAIJAN
 * Add new field to TbCaseAZ - date registration in the system
 ****************************************************************/

ALTER TABLE `etbmanager`.`tbcaseaz` ADD COLUMN `systemDate` DATE NULL DEFAULT NULL  AFTER `unicalID` ;

