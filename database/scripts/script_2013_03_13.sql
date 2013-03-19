/***************************************************************
 * 13-mar-2013
 * By A.M.
 * 
 * For all countryes. Add new fields
 * 
****************************************************************/

ALTER TABLE `etbmanager`.`batch` 
ADD COLUMN `registCardNumber` VARCHAR(30) NULL DEFAULT NULL  AFTER `RECEIVINGITEM_ID` , 
ADD COLUMN `registCardBeginDate` DATE NULL DEFAULT NULL  AFTER `registCardNumber` , 
ADD COLUMN `registCardEndDate` DATE NULL DEFAULT NULL  AFTER `registCardBeginDate`;

