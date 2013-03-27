/***************************************************************
 * 27-mar-2013
 * By A.M.
 * 
 * For all countries. Add new field
 * 
****************************************************************/

ALTER TABLE `etbmanager`.`transfer` ADD COLUMN `consignmentNumber` VARCHAR(45) NULL DEFAULT NULL  AFTER `USER_TO_ID` ;

