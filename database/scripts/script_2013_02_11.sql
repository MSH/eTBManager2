/***************************************************************
 * 11-feb-2013
 * By A.M.
 * 
 * AZ only!!! Add new fields to TbCaseAZ
 * 
****************************************************************/
ALTER TABLE `etbmanager`.`tbcaseaz` 
ADD COLUMN `colPrevTreatUnknown` BIT(1) NULL DEFAULT b'0'  AFTER `inEIDSSDate` ;




