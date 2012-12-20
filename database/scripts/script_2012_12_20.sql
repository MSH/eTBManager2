/***************************************************************
 * 11-dec-2012
 * By A.M.
 * 
 * UA only!!! Add new fields in form TB01 and TB01 Appendix
 * 
****************************************************************/
ALTER TABLE `etbmanager`.`casedataua` 
ADD COLUMN `testHIVdate` DATE NULL DEFAULT NULL  AFTER `REGISTRATION_CATEGORY` , 
ADD COLUMN `treatARTdate` DATE NULL DEFAULT NULL  AFTER `testHIVdate` , 
ADD COLUMN `kotrymoksTreatDate` DATE NULL DEFAULT NULL  AFTER `treatARTdate` ,
ADD COLUMN `dischargeDate` DATE NULL DEFAULT NULL  AFTER `kotrymoksTreatDate` ;



