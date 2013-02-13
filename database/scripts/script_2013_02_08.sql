/***************************************************************
 * 08-feb-2013
 * By A.M.
 * 
 * UA only!!! Add new fields to CaseDataUA
 * 
****************************************************************/
ALTER TABLE `etbmanager`.`casedataua` 
ADD COLUMN `hospitalizationDate2` DATE NULL DEFAULT NULL  AFTER `treatARTdate` , 
ADD COLUMN `hospitalizationDate3` DATE NULL DEFAULT NULL  AFTER `hospitalizationDate2` , 
ADD COLUMN `hospitalizationDate4` DATE NULL DEFAULT NULL  AFTER `hospitalizationDate3` , 
ADD COLUMN `hospitalizationDate5` DATE NULL DEFAULT NULL  AFTER `hospitalizationDate4` , 
ADD COLUMN `dischargeDate2` DATE NULL DEFAULT NULL  AFTER `hospitalizationDate5` , 
ADD COLUMN `dischargeDate3` DATE NULL DEFAULT NULL  AFTER `dischargeDate2` , 
ADD COLUMN `dischargeDate4` DATE NULL DEFAULT NULL  AFTER `dischargeDate3` , 
ADD COLUMN `dischargeDate5` DATE NULL DEFAULT NULL  AFTER `dischargeDate4` ;



